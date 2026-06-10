package ru.rsreu.festivalmanager.festival.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketTypeInstanceCreateRequest {
    @JsonProperty("ticket_type_id")
    private Long ticketTypeId;

    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private BigDecimal price;
}
