package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketPurchaseRequest {
    @JsonProperty("ticket_type_instance_id")
    @NotNull(message = "ID типа билета обязателен")
    private Long ticketTypeInstanceId;

    @JsonProperty("quantity")
    @NotNull(message = "Количество билетов обязательно")
    @Min(value = 1, message = "Минимум 1 билет")
    private Integer quantity;
}
