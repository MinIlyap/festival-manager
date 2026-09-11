package ru.rsreu.festivalmanager.festival.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.festival.dto.ticket.TicketResponse;
import ru.rsreu.festivalmanager.festival.dto.ticket.TicketTypeInstanceResponse;
import ru.rsreu.festivalmanager.festival.entity.ticket.Ticket;
import ru.rsreu.festivalmanager.festival.entity.ticket.TicketTypeInstance;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketMapper {

    /**
     * The remaining count is derived once per festival from the sold ticket
     * total, so it is passed in rather than read off the instance.
     */
    @Mapping(target = "name", source = "instance.ticketType.name")
    @Mapping(target = "remaining", source = "remaining")
    TicketTypeInstanceResponse toTicketTypeInstanceResponse(TicketTypeInstance instance, int remaining);

    @Mapping(target = "ticketId", source = "id")
    @Mapping(target = "festivalLocationId", source = "ticketTypeInstance.festivalLocation.id")
    @Mapping(target = "festivalName", source = "ticketTypeInstance.festivalLocation.festivalInstance.festival.name")
    @Mapping(target = "ticketType", source = "ticketTypeInstance.ticketType.name")
    TicketResponse toTicketResponse(Ticket ticket);
}
