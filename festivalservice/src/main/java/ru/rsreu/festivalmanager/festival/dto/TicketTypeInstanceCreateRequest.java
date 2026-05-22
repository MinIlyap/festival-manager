package ru.rsreu.festivalmanager.festival.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketTypeInstanceCreateRequest {
    private Long ticketTypeId;
    private Long festivalLocationId;
    private String description;
    private BigDecimal price;
}
