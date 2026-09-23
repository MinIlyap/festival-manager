package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistration;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistrationId;
import ru.rsreu.festivalmanager.event.repository.FestivalEventRepository;
import ru.rsreu.festivalmanager.event.repository.UserEventRegistrationRepository;
import ru.rsreu.festivalmanager.event.client.FestivalServiceClient;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {
    private final FestivalEventRepository festivalEventRepository;
    private final UserEventRegistrationRepository registrationRepository;
    private final FestivalServiceClient festivalServiceClient;
    private final ApplicationContext applicationContext;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() {
        return "Bearer " + systemToken;
    }

    public void registerUserToEvent(Long festivalEventId, Long userId) {
        if (registrationRepository
                .existsByUserEventRegistrationId_FestivalEvent_IdAndUserEventRegistrationId_UserId(festivalEventId,
                        userId)) {
            throw new IllegalStateException("Вы уже зарегистрированы на это мероприятие");
        }

        FestivalEvent festivalEvent = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        Boolean hasTicket = festivalServiceClient.checkUserHasValidTicket(
                getSystemToken(), festivalEvent.getFestivalLocationId(), userId
        );

        if (Boolean.FALSE.equals(hasTicket)) {
            throw new IllegalStateException("Для регистрации необходим действующий билет на этот фестиваль");
        }

        Integer maxPeople = festivalEvent.getEventInstance().getMaxNumberPeople();

        applicationContext.getBean(EventRegistrationService.class)
                .executeRegistrationWithLock(festivalEventId, userId, maxPeople);
    }

    @Transactional
    public void executeRegistrationWithLock(Long festivalEventId, Long userId, Integer maxPeople) {

        FestivalEvent foundedFestivalEvent = festivalEventRepository.findByIdForUpdate(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        if (registrationRepository
                .existsByUserEventRegistrationId_FestivalEvent_IdAndUserEventRegistrationId_UserId(festivalEventId, userId)) {
            throw new IllegalStateException("Вы уже зарегистрированы на это мероприятие");
        }

        long currentParticipants = registrationRepository.countByUserEventRegistrationId_FestivalEvent_Id(festivalEventId);

        if (maxPeople != null && currentParticipants >= maxPeople) {
            throw new IllegalStateException("Регистрация закрыта: свободных мест больше нет");
        }

        UserEventRegistration registration = new UserEventRegistration();
        registration.setUserEventRegistrationId(new UserEventRegistrationId(foundedFestivalEvent, userId));

        registrationRepository.save(registration);
    }
}
