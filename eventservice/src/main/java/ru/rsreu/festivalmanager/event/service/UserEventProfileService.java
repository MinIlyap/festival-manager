package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.dto.event.FestivalEventResponse;
import ru.rsreu.festivalmanager.event.dto.event.UserFestivalEventsResponse;
import ru.rsreu.festivalmanager.event.dto.reward.UserFestivalRewardsResponse;
import ru.rsreu.festivalmanager.event.dto.reward.UserRewardResponse;
import ru.rsreu.festivalmanager.event.entity.event.EventType;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.UserEventRegistration;
import ru.rsreu.festivalmanager.event.entity.reward.RewardDistribution;
import ru.rsreu.festivalmanager.event.repository.*;
import ru.rsreu.festivalmanager.event.mapper.EventMapper;
import ru.rsreu.festivalmanager.event.mapper.RewardMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEventProfileService {
    private final FestivalEventRepository festivalEventRepository;
    private final EventMapper eventMapper;
    private final RewardMapper rewardMapper;
    private final UserEventRegistrationRepository registrationRepository;
    private final RewardDistributionRepository rewardDistributionRepository;

    @Transactional(readOnly = true)
    public List<FestivalEventResponse> getEventsForFestival(Long festivalLocationId, EventType type) {
        return festivalEventRepository.findByLocationAndType(festivalLocationId, type)
                .stream()
                .map(eventMapper::toFestivalEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserFestivalEventsResponse> getMyAttendedEvents(Long userId) {
        List<UserEventRegistration> registrations = registrationRepository.findAllByUserEventRegistrationId_UserId(userId);

        Map<Long, List<FestivalEvent>> groupedEvents = registrations.stream()
                .map(reg -> reg.getUserEventRegistrationId().getFestivalEvent())
                .collect(Collectors.groupingBy(FestivalEvent::getFestivalLocationId));

        return groupedEvents.entrySet().stream()
                .map(entry -> new UserFestivalEventsResponse(
                        entry.getKey(),
                        entry.getValue().stream().map(eventMapper::toFestivalEventResponse).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserFestivalRewardsResponse> getMyRewards(Long userId) {
        List<RewardDistribution> distributions = rewardDistributionRepository.findAllByRewardDistributionId_UserId(userId);

        Map<Long, List<RewardDistribution>> groupedDistributions = distributions.stream()
                .collect(Collectors.groupingBy(dist ->
                        dist.getRewardDistributionId().getEventReward().getFestivalEvent().getFestivalLocationId()
                ));

        return groupedDistributions.entrySet().stream()
                .map(entry -> {
                    Long festivalId = entry.getKey();
                    List<UserRewardResponse> rewards = entry.getValue().stream()
                            .map(dist -> rewardMapper.toUserRewardResponse(dist.getRewardDistributionId().getEventReward()))
                            .collect(Collectors.toList());

                    return new UserFestivalRewardsResponse(festivalId, rewards);
                })
                .collect(Collectors.toList());
    }

}
