package ru.rsreu.festivalmanager.festival.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TicketTypeRequest {
    @NotNull(message = "ID типа билета обязателен")
    @JsonProperty("ticket_type_id")
    private Long ticketTypeId;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Цена обязательна")
    @JsonProperty("price")
    private BigDecimal price;
}
