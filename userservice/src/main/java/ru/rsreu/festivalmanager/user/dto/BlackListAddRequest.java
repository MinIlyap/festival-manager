package ru.rsreu.festivalmanager.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlackListAddRequest {
    @NotBlank(message = "Причина блокировки обязательна")
    private String reason;

    @NotNull(message = "Укажите количество дней")
    @Min(value = 1, message = "Минимум 1 день блокировки")
    private Integer days;
}
