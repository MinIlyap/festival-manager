package ru.rsreu.festivalmanager.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlackListAddRequest {
    @NotBlank(message = "Причина блокировки обязательна")
    @JsonProperty("reason")
    private String reason;

    @NotNull(message = "Укажите количество дней")
    @Min(value = 10, message = "Минимум 1 день блокировки")
    @JsonProperty("days")
    private Integer days;
}
