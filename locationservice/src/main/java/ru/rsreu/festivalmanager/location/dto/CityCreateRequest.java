package ru.rsreu.festivalmanager.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CityCreateRequest {
    @NotBlank(message = "Название города обязательно")
    @JsonProperty("name")
    private String name;
}
