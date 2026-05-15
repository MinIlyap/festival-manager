package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.dto.FestivalEventResponse;
import ru.rsreu.festivalmanager.event.dto.UserFestivalEventsResponse;
import ru.rsreu.festivalmanager.event.dto.UserFestivalRewardsResponse;
import ru.rsreu.festivalmanager.event.dto.UserRewardResponse;
import ru.rsreu.festivalmanager.event.entity.*;
import ru.rsreu.festivalmanager.event.entity.event.Event;
import ru.rsreu.festivalmanager.event.entity.event.EventInstance;
import ru.rsreu.festivalmanager.event.entity.event.EventType;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistration;
import ru.rsreu.festivalmanager.event.entity.reward.EventReward;
import ru.rsreu.festivalmanager.event.entity.reward.RewardDistribution;
import ru.rsreu.festivalmanager.event.entity.reward.RewardInstance;
import ru.rsreu.festivalmanager.event.repository.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEventProfileService {

    private final FestivalEventRepository festivalEventRepository;
    private final UserEventRegistrationRepository registrationRepository;
    private final RewardDistributionRepository rewardDistributionRepository;

    /**
     * 1. Просмотр расписания фестиваля (с фильтром по типу)
     */
    @Transactional(readOnly = true)
    public List<FestivalEventResponse> getEventsForFestival(Long festivalLocationId, EventType type) {
        return festivalEventRepository.findByLocationAndType(festivalLocationId, type)
                .stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    /**
     * 2. Посещенные мероприятия (сгруппированные по фестивалям)
     */
    @Transactional(readOnly = true)
    public List<UserFestivalEventsResponse> getMyAttendedEvents(Long userId) {
        List<UserEventRegistration> registrations = registrationRepository.findAllByUserEventRegistrationId_UserId(userId);

        // Группируем мероприятия (FestivalEvent) по ID локации фестиваля
        Map<Long, List<FestivalEvent>> groupedEvents = registrations.stream()
                .map(reg -> reg.getUserEventRegistrationId().getFestivalEvent())
                .collect(Collectors.groupingBy(FestivalEvent::getFestivalLocationId));

        // Превращаем Map в список красивых DTO
        return groupedEvents.entrySet().stream()
                .map(entry -> new UserFestivalEventsResponse(
                        entry.getKey(),
                        entry.getValue().stream().map(this::mapToEventResponse).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 3. Полученные награды (сгруппированные по фестивалям)
     */
    @Transactional(readOnly = true)
    public List<UserFestivalRewardsResponse> getMyRewards(Long userId) {
        List<RewardDistribution> distributions = rewardDistributionRepository.findAllByRewardDistributionId_UserId(userId);

        // Группируем награды по ID локации фестиваля
        Map<Long, List<RewardDistribution>> groupedDistributions = distributions.stream()
                .collect(Collectors.groupingBy(dist ->
                        dist.getRewardDistributionId().getEventReward().getFestivalEvent().getFestivalLocationId()
                ));

        return groupedDistributions.entrySet().stream()
                .map(entry -> {
                    Long festivalId = entry.getKey();
                    List<UserRewardResponse> rewards = entry.getValue().stream()
                            .map(dist -> mapToRewardResponse(dist.getRewardDistributionId().getEventReward()))
                            .collect(Collectors.toList());

                    return new UserFestivalRewardsResponse(festivalId, rewards);
                })
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы маппинга ---

    private FestivalEventResponse mapToEventResponse(FestivalEvent fe) {
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

    private UserRewardResponse mapToRewardResponse(EventReward eventReward) {
        RewardInstance ri = eventReward.getRewardInstance();

        return UserRewardResponse.builder()
                .eventRewardId(eventReward.getId())
                .eventName(eventReward.getFestivalEvent().getEventInstance().getEvent().getName())
                .rewardName(ri.getReward().getName())
                .description(ri.getDescription())
                .place(ri.getPlace())
                .pointNumber(ri.getPointNumber())
                .build();
    }
}