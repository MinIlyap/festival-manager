package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TicketTypeRequest {
    @JsonProperty("ticket_type_id")
    @NotNull(message = "ID типа билета обязателен")
    private Long ticketTypeId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    @NotNull(message = "Цена обязательна")
    private BigDecimal price;
}