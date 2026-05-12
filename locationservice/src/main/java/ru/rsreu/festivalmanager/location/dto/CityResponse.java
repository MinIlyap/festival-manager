package ru.rsreu.festivalmanager.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
}
