package ru.rsreu.festivalmanager.festival.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketPurchaseRequest {
    @NotNull(message = "ID типа билета обязателен")
    @JsonProperty("ticket_type_instance_id")
    private Long ticketTypeInstanceId;

    @NotNull(message = "Количество билетов обязательно")
    @Min(value = 1, message = "Минимум 1 билет")
    @JsonProperty("quantity")
    private Integer quantity;
}
