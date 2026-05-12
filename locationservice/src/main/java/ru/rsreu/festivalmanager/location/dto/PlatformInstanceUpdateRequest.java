package ru.rsreu.festivalmanager.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlatformInstanceUpdateRequest {
    @NotBlank(message = "Улица обязательна")
    @JsonProperty("street")
    private String street;

    @NotBlank(message = "Номер дома обязателен")
    @JsonProperty("house")
    private String house;
}
