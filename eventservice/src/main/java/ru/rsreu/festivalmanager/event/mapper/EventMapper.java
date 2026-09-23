package ru.rsreu.festivalmanager.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.event.dto.event.CommandWithMembersResponse;
import ru.rsreu.festivalmanager.event.dto.event.FestivalEventResponse;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;
import ru.rsreu.festivalmanager.event.entity.registration.Command;
import ru.rsreu.festivalmanager.event.entity.registration.CommandMember;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    @Mapping(target = "festivalEventId", source = "id")
    @Mapping(target = "eventName", source = "eventInstance.event.name")
    @Mapping(target = "type", source = "eventInstance.eventType")
    @Mapping(target = "description", source = "eventInstance.description")
    @Mapping(target = "dateStart", source = "eventInstance.dateStart")
    @Mapping(target = "timeStart", source = "eventInstance.timeStart")
    @Mapping(target = "approximateDuration", source = "eventInstance.approximateDuration")
    @Mapping(target = "isCommand", source = "eventInstance.isCommand")
    @Mapping(target = "maxNumberPeople", source = "eventInstance.maxNumberPeople")
    @Mapping(target = "maxPeopleCommand", source = "eventInstance.maxPeopleCommand")
    @Mapping(target = "organizerCompanyId", source = "eventInstance.companyId")
    @Mapping(target = "companyBrandName", ignore = true)
    FestivalEventResponse toFestivalEventResponse(FestivalEvent festivalEvent);

    @Mapping(target = "commandId", source = "id")
    @Mapping(target = "commandName", source = "name")
    @Mapping(target = "memberUserIds", source = "members")
    CommandWithMembersResponse toCommandWithMembersResponse(Command command);

    default Long toUserId(CommandMember member) {
        return member.getCommandMemberId().getUserId();
    }
}
