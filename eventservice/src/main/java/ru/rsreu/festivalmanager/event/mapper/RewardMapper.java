package ru.rsreu.festivalmanager.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.event.dto.reward.EventRewardResponse;
import ru.rsreu.festivalmanager.event.dto.reward.UserRewardResponse;
import ru.rsreu.festivalmanager.event.entity.reward.EventReward;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RewardMapper {

    @Mapping(target = "eventRewardId", source = "id")
    @Mapping(target = "rewardId", source = "rewardInstance.reward.id")
    @Mapping(target = "rewardName", source = "rewardInstance.reward.name")
    @Mapping(target = "description", source = "rewardInstance.description")
    @Mapping(target = "place", source = "rewardInstance.place")
    @Mapping(target = "pointNumber", source = "rewardInstance.pointNumber")
    EventRewardResponse toEventRewardResponse(EventReward eventReward);

    @Mapping(target = "eventRewardId", source = "id")
    @Mapping(target = "eventName", source = "festivalEvent.eventInstance.event.name")
    @Mapping(target = "rewardName", source = "rewardInstance.reward.name")
    @Mapping(target = "description", source = "rewardInstance.description")
    @Mapping(target = "place", source = "rewardInstance.place")
    @Mapping(target = "pointNumber", source = "rewardInstance.pointNumber")
    UserRewardResponse toUserRewardResponse(EventReward eventReward);
}
