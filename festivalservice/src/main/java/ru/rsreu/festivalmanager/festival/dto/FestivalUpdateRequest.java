package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class FestivalUpdateRequest {

    @JsonProperty("festival_name")
    @NotBlank(message = "Название фестиваля обязательно")
    private String festivalName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("date_start")
    @NotNull(message = "Дата начала обязательна")
    private LocalDate dateStart;

    @JsonProperty("time_start")
    @NotNull(message = "Время начала обязательно")
    private LocalTime timeStart;

    @JsonProperty("date_end")
    @NotNull(message = "Дата окончания обязательна")
    private LocalDate dateEnd;

    @JsonProperty("time_end")
    @NotNull(message = "Время окончания обязательно")
    private LocalTime timeEnd;

    @JsonProperty("max_people_number")
    @NotNull(message = "Максимальное количество посетителей обязательно")
    private Integer maxPeopleNumber;

    @JsonProperty("age_limit_id")
    @NotNull(message = "Возрастное ограничение обязательно")
    private Integer ageLimitId;
}
