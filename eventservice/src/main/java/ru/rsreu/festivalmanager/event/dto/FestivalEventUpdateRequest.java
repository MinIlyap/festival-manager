package ru.rsreu.festivalmanager.event.dto;

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
public class FestivalEventUpdateRequest {
    @NotNull(message = "Тип мероприятия обязателен")
    private EventType type;

    private String description;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate dateStart;

    @NotNull(message = "Время начала обязательно")
    private LocalTime timeStart;

    private Integer approximateDuration;

    @NotNull(message = "Флаг командного мероприятия обязателен")
    private Boolean isCommand;

    @NotNull(message = "Максимальное количество людей обязательно")
    private Integer maxNumberPeople;

    private Integer maxPeopleCommand;

    private Long organizerCompanyId;
}
