package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.event.dto.FestivalEventCreateRequest;
import ru.rsreu.festivalmanager.event.dto.FestivalEventResponse;
import ru.rsreu.festivalmanager.event.entity.event.Event;
import ru.rsreu.festivalmanager.event.entity.event.EventInstance;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.repository.EventInstanceRepository;
import ru.rsreu.festivalmanager.event.repository.EventRepository;
import ru.rsreu.festivalmanager.event.repository.FestivalEventRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FestivalEventService {

    private final FestivalEventRepository festivalEventRepository;
    private final FestivalServiceClient festivalServiceClient;
    private final EventRepository eventRepository;
    private final EventInstanceRepository eventInstanceRepository;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() {
        return "Bearer " + systemToken;
    }

    /**
     * Поиск мероприятий с фильтрацией и пагинацией
     */
    @Transactional(readOnly = true)
    public Page<FestivalEventResponse> searchFestivalEvents(
            String name, Long cityId, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        List<Long> festivalLocationIds = null;

        // Если ищем по городу, идем в Festival Service за списком ID фестивалей
        if (cityId != null) {
            festivalLocationIds = festivalServiceClient.getFestivalLocationIdsByCity(getSystemToken(), cityId);

            // Если в этом городе нет фестивалей, сразу возвращаем пустую страницу
            if (festivalLocationIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        // В репозитории нужно написать @Query, который учитывает name, даты и список IN (festivalLocationIds)
        return festivalEventRepository.searchWithFilters(name, startDate, endDate, festivalLocationIds, pageable)
                .map(this::mapToResponse); // маппинг в DTO
    }

    /**
     * БЕЗОПАСНОЕ ОТКРЕПЛЕНИЕ МЕРОПРИЯТИЯ ОТ ФЕСТИВАЛЯ
     */
    @Transactional
    public void detachEventFromFestival(Long festivalEventId) {
        FestivalEvent festivalEvent = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        // 1. Идем в Festival Service и проверяем статус фестиваля
        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), festivalEvent.getFestivalLocationId()
        );

        // 2. Реализуем твое правило: если фестиваль видим - откреплять нельзя!
        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException(
                    "Нельзя открепить мероприятие: фестиваль уже опубликован и доступен пользователям!"
            );
        }

        // 3. Если фестиваль еще в черновике - спокойно удаляем связь
        festivalEventRepository.delete(festivalEvent);
    }

    /**
     * Создание экземпляра мероприятия и привязка к фестивалю
     */
    @Transactional
    public Long createFestivalEvent(Long companyId, FestivalEventCreateRequest request) {

        // 1. Ищем базовое мероприятие в словаре
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Базовое мероприятие не найдено"));

        if (Boolean.TRUE.equals(request.getIsCommand()) && request.getMaxPeopleCommand() == null) {
            throw new IllegalArgumentException("Для командного мероприятия нужно указать лимит людей в команде");
        }

        // 2. Ищем точную копию инстанса ИЛИ создаем новую
        EventInstance instance = eventInstanceRepository.findIdenticalInstance(
                request.getEventId(),
                companyId,
                request.getType(),
                request.getDateStart(),
                request.getTimeStart(),
                request.getIsCommand(),
                request.getMaxNumberPeople(),
                request.getDescription(),
                request.getApproximateDuration(),
                request.getMaxPeopleCommand()
        ).orElseGet(() -> {
            // Если не нашли - собираем новый
            EventInstance newInstance = EventInstance.builder()
                    .event(event)
                    .companyId(companyId)
                    .eventType(request.getType())
                    .description(request.getDescription())
                    .dateStart(request.getDateStart())
                    .timeStart(request.getTimeStart())
                    .approximateDuration(request.getApproximateDuration())
                    .isCommand(request.getIsCommand())
                    .maxNumberPeople(request.getMaxNumberPeople())
                    .maxPeopleCommand(request.getMaxPeopleCommand())
                    .build();
            return eventInstanceRepository.save(newInstance);
        });

        // 3. Привязываем инстанс к локации фестиваля
        FestivalEvent festivalEvent = FestivalEvent.builder()
                .festivalLocationId(request.getFestivalLocationId())
                .eventInstance(instance)
                .build();

        return festivalEventRepository.save(festivalEvent).getId();
    }

    private FestivalEventResponse mapToResponse(FestivalEvent fe) {
        EventInstance ei = fe.getEventInstance();
        Event e = ei.getEvent();

        return FestivalEventResponse.builder()
                .festivalEventId(fe.getId())
                .festivalLocationId(fe.getFestivalLocationId())
                .eventName(e.getName())
                .type(ei.getEventType())
                .description(ei.getDescription())
                .dateStart(ei.getDateStart())
                .timeStart(ei.getTimeStart())
                .approximateDuration(ei.getApproximateDuration())
                .isCommand(ei.getIsCommand())
                .maxNumberPeople(ei.getMaxNumberPeople())
                .maxPeopleCommand(ei.getMaxPeopleCommand())
                .createdAt(fe.getCreatedAt())
                .build();
    }
}
