package ru.rsreu.festivalmanager.festival.dto.festival;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlatformInfoDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("platform_name")
    private String platformName;

    @JsonProperty("city")
    private CityInfoDto city;

    @JsonProperty("street")
    private String street;

    @JsonProperty("house")
    private String house;

    @Data
    @NoArgsConstructor
    public static class CityInfoDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;
    }
}
