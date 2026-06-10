package ru.rsreu.festivalmanager.festival.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketTypeInstanceUpdateRequest {
    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private BigDecimal price;
}
