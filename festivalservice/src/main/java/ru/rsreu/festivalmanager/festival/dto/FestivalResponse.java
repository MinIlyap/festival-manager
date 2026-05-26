package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class FestivalResponse {
    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("date_start")
    private LocalDate dateStart;

    @JsonProperty("time_start")
    private LocalTime timeStart;

    @JsonProperty("date_end")
    private LocalDate dateEnd;

    @JsonProperty("time_end")
    private LocalTime timeEnd;

    @JsonProperty("platform_instance_id")
    private Long platformInstanceId;

    @JsonProperty("is_visible")
    private Boolean isVisible;

    @JsonProperty("age_rating")
    private Integer ageRating;

    @JsonProperty("age_limit_id")
    private Integer ageLimitId;

    @JsonProperty("max_people_number")
    private Integer maxPeopleNumber;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("platform_name")
    private String platformName;

    @JsonProperty("address")
    private String address;
}
