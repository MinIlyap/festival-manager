package ru.rsreu.festivalmanager.event.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rsreu.festivalmanager.event.entity.event.EventType;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalEventCreateRequest {
    @NotNull(message = "ID базового мероприятия обязательно")
    @JsonProperty("event_id")
    private Long eventId;

    @NotNull(message = "ID локации фестиваля обязательно")
    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @NotNull(message = "Тип мероприятия обязателен")
    @JsonProperty("type")
    private EventType type;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Дата начала обязательна")
    @JsonProperty("date_start")
    private LocalDate dateStart;

    @NotNull(message = "Время начала обязательно")
    @JsonProperty("time_start")
    private LocalTime timeStart;

    @JsonProperty("approximate_duration")
    private Integer approximateDuration;

    @NotNull(message = "Флаг командного мероприятия обязателен")
    @JsonProperty("is_command")
    private Boolean isCommand;

    @NotNull(message = "Максимальное количество людей обязательно")
    @JsonProperty("max_number_people")
    private Integer maxNumberPeople;

    @JsonProperty("max_people_command")
    private Integer maxPeopleCommand;

    @JsonProperty("organizer_company_id")
    private Long organizerCompanyId;
}
