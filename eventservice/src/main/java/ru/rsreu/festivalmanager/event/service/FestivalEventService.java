package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.event.dto.event.CompanyBriefDto;
import ru.rsreu.festivalmanager.event.dto.event.FestivalEventCreateRequest;
import ru.rsreu.festivalmanager.event.dto.event.FestivalEventResponse;
import ru.rsreu.festivalmanager.event.dto.event.FestivalEventUpdateRequest;
import ru.rsreu.festivalmanager.event.entity.event.Event;
import ru.rsreu.festivalmanager.event.entity.event.EventInstance;
import ru.rsreu.festivalmanager.event.entity.event.EventType;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.repository.EventInstanceRepository;
import ru.rsreu.festivalmanager.event.mapper.EventMapper;
import ru.rsreu.festivalmanager.event.repository.EventRepository;
import ru.rsreu.festivalmanager.event.repository.FestivalEventRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalEventService {
    private final FestivalEventRepository festivalEventRepository;
    private final EventMapper eventMapper;
    private final FestivalServiceClient festivalServiceClient;
    private final EventRepository eventRepository;
    private final EventInstanceRepository eventInstanceRepository;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getSystemToken() {
        return "Bearer " + systemToken;
    }

    @Transactional(readOnly = true)
    public Page<FestivalEventResponse> searchFestivalEvents(
            String name, Long cityId, LocalDate startDate, LocalDate endDate, EventType type, Pageable pageable) {

        List<Long> festivalLocationIds;

        if (cityId != null) {
            festivalLocationIds = festivalServiceClient.getFestivalLocationIdsByCity(getSystemToken(), cityId);
        } else {
            festivalLocationIds = festivalServiceClient.getAllVisibleFestivalLocationIds(getSystemToken());
        }

        if (festivalLocationIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return festivalEventRepository.searchWithFilters(name, startDate, endDate, festivalLocationIds, type, pageable)
                .map(eventMapper::toFestivalEventResponse);
    }

    @Transactional
    public void updateFestivalEvent(Long festivalEventId, Long companyId, FestivalEventUpdateRequest request) {
        FestivalEvent festivalEvent = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), festivalEvent.getFestivalLocationId()
        );
        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException("Нельзя редактировать мероприятие: фестиваль уже опубликован");
        }

        Long eventCompanyId = festivalEvent.getEventInstance().getCompanyId();
        boolean isEventOrganizer = companyId.equals(eventCompanyId);
        boolean isFestivalOrganizer = Boolean.TRUE.equals(
                festivalServiceClient.isCompanyFestivalOrganizer(getSystemToken(), festivalEvent.getFestivalLocationId(), companyId)
        );
        if (!isEventOrganizer && !isFestivalOrganizer) {
            throw new IllegalStateException("Редактировать мероприятие может только его компания-организатор или организатор фестиваля");
        }

        if (Boolean.TRUE.equals(request.getIsCommand()) && request.getMaxPeopleCommand() == null) {
            throw new IllegalArgumentException("Для командного мероприятия нужно указать лимит людей в команде");
        }

        EventInstance instance = festivalEvent.getEventInstance();
        instance.setEventType(request.getType());
        instance.setDescription(request.getDescription());
        instance.setDateStart(request.getDateStart());
        instance.setTimeStart(request.getTimeStart());
        instance.setApproximateDuration(request.getApproximateDuration());
        instance.setIsCommand(request.getIsCommand());
        instance.setMaxNumberPeople(request.getMaxNumberPeople());
        instance.setMaxPeopleCommand(request.getMaxPeopleCommand());
        if (request.getOrganizerCompanyId() != null) {
            instance.setCompanyId(request.getOrganizerCompanyId());
        }

        eventInstanceRepository.save(instance);
    }

    @Transactional
    public void detachEventFromFestival(Long festivalEventId, Long companyId) {
        FestivalEvent festivalEvent = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), festivalEvent.getFestivalLocationId()
        );

        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException(
                    "Нельзя открепить мероприятие: фестиваль уже опубликован и доступен пользователям!"
            );
        }

        Long eventCompanyId = festivalEvent.getEventInstance().getCompanyId();
        boolean isEventOrganizer = companyId.equals(eventCompanyId);
        boolean isFestivalOrganizer = Boolean.TRUE.equals(
                festivalServiceClient.isCompanyFestivalOrganizer(getSystemToken(), festivalEvent.getFestivalLocationId(), companyId)
        );
        if (!isEventOrganizer && !isFestivalOrganizer) {
            throw new IllegalStateException("Удалить мероприятие может только его компания-организатор или организатор фестиваля");
        }

        festivalEventRepository.delete(festivalEvent);
    }

    @Transactional
    public Long createFestivalEvent(Long companyId, FestivalEventCreateRequest request) {
        Boolean isVisible = festivalServiceClient.isFestivalVisible(
                getSystemToken(), request.getFestivalLocationId()
        );
        if (Boolean.TRUE.equals(isVisible)) {
            throw new IllegalStateException("Нельзя добавить мероприятие: фестиваль уже опубликован");
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Базовое мероприятие не найдено"));

        if (Boolean.TRUE.equals(request.getIsCommand()) && request.getMaxPeopleCommand() == null) {
            throw new IllegalArgumentException("Для командного мероприятия нужно указать лимит людей в команде");
        }

        Long organizerCompanyId = request.getOrganizerCompanyId() != null ? request.getOrganizerCompanyId() : companyId;

        EventInstance instance = eventInstanceRepository.findIdenticalInstance(
                request.getEventId(),
                organizerCompanyId,
                request.getType(),
                request.getDateStart(),
                request.getTimeStart(),
                request.getIsCommand(),
                request.getMaxNumberPeople(),
                request.getDescription(),
                request.getApproximateDuration(),
                request.getMaxPeopleCommand()
        ).orElseGet(() -> {
            EventInstance newInstance = EventInstance.builder()
                    .event(event)
                    .companyId(organizerCompanyId)
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

        FestivalEvent festivalEvent = FestivalEvent.builder()
                .festivalLocationId(request.getFestivalLocationId())
                .eventInstance(instance)
                .build();

        return festivalEventRepository.save(festivalEvent).getId();
    }

    @Transactional(readOnly = true)
    public FestivalEventResponse getEventById(Long festivalEventId) {
        FestivalEvent festivalEvent = festivalEventRepository.findById(festivalEventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));
        FestivalEventResponse response = eventMapper.toFestivalEventResponse(festivalEvent);
        if (festivalEvent.getEventInstance().getCompanyId() != null) {
            try {
                CompanyBriefDto company =
                        festivalServiceClient.getCompanyById(getSystemToken(), festivalEvent.getEventInstance().getCompanyId());
                if (company != null) {
                    response.setCompanyBrandName(
                            company.getBrandName() != null ? company.getBrandName() : company.getFullName()
                    );
                }
            } catch (Exception ignored) {}
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<FestivalEventResponse> getEventsByCompany(Long companyId) {
        return festivalEventRepository.findAllByEventInstance_CompanyId(companyId)
                .stream()
                .map(eventMapper::toFestivalEventResponse)
                .toList();
    }

}
