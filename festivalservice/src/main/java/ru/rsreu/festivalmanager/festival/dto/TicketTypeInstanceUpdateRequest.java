package ru.rsreu.festivalmanager.festival.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketTypeInstanceUpdateRequest {
    private String description;
    private BigDecimal price;
}
