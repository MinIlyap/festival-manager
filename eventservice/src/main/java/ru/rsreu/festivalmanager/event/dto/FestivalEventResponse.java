package ru.rsreu.festivalmanager.event.dto;

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
    private Long festivalEventId;
    private Long festivalLocationId;

    private String eventName;

    private EventType type;
    private String description;
    private LocalDate dateStart;
    private LocalTime timeStart;
    private Integer approximateDuration;
    private Boolean isCommand;
    private Integer maxNumberPeople;
    private Integer maxPeopleCommand;

    private Long organizerCompanyId;

    private String companyBrandName;

    private OffsetDateTime createdAt;
}
