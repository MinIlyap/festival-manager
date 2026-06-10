package ru.rsreu.festivalmanager.event.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rsreu.festivalmanager.event.entity.event.EventType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalEventResponse {
    @JsonProperty("festival_event_id")
    private Long festivalEventId;

    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("event_name")
    private String eventName;

    @JsonProperty("type")
    private EventType type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("date_start")
    private LocalDate dateStart;

    @JsonProperty("time_start")
    private LocalTime timeStart;

    @JsonProperty("approximate_duration")
    private Integer approximateDuration;

    @JsonProperty("is_command")
    private Boolean isCommand;

    @JsonProperty("max_number_people")
    private Integer maxNumberPeople;

    @JsonProperty("max_people_command")
    private Integer maxPeopleCommand;

    @JsonProperty("organizer_company_id")
    private Long organizerCompanyId;

    @JsonProperty("company_brand_name")
    private String companyBrandName;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
