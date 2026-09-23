package ru.rsreu.festivalmanager.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlatformInstanceResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("platform_name")
    private String platformName;

    @JsonProperty("city")
    private CityResponse city;

    @JsonProperty("street")
    private String street;

    @JsonProperty("house")
    private String house;
}
