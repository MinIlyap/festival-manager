package ru.rsreu.festivalmanager.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlatformInstanceCreateRequest {
    @NotBlank(message = "Название площадки обязательно")
    @JsonProperty("platform_name")
    private String platformName;

    @NotNull(message = "ID города обязателен")
    @JsonProperty("city_id")
    private Long cityId;

    @NotBlank(message = "Улица обязательна")
    @JsonProperty("street")
    private String street;

    @NotBlank(message = "Номер дома обязателен")
    @JsonProperty("house")
    private String house;
}
