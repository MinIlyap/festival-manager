package ru.rsreu.festivalmanager.festival.dto.festival;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class FestivalUpdateRequest {
    @NotBlank(message = "Название фестиваля обязательно")
    @JsonProperty("festival_name")
    private String festivalName;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Дата начала обязательна")
    @JsonProperty("date_start")
    private LocalDate dateStart;

    @NotNull(message = "Время начала обязательно")
    @JsonProperty("time_start")
    private LocalTime timeStart;

    @NotNull(message = "Дата окончания обязательна")
    @JsonProperty("date_end")
    private LocalDate dateEnd;

    @NotNull(message = "Время окончания обязательно")
    @JsonProperty("time_end")
    private LocalTime timeEnd;

    @NotNull(message = "Максимальное количество посетителей обязательно")
    @JsonProperty("max_people_number")
    private Integer maxPeopleNumber;

    @NotNull(message = "Возрастное ограничение обязательно")
    @JsonProperty("age_limit_id")
    private Integer ageLimitId;
}
