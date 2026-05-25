package ru.rsreu.festivalmanager.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CityNameResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
}
