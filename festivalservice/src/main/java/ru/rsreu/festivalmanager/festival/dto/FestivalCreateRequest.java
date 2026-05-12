package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class FestivalCreateRequest {

    @JsonProperty("festival_name")
    @NotBlank(message = "Название фестиваля обязательно")
    private String festivalName;

    @JsonProperty("age_limit_id")
    @NotNull(message = "Возрастное ограничение обязательно")
    private Integer ageLimitId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("date_start")
    @NotNull
    private LocalDate dateStart;

    @JsonProperty("time_start")
    @NotNull
    private LocalTime timeStart;

    @JsonProperty("date_end")
    @NotNull
    private LocalDate dateEnd;

    @JsonProperty("time_end")
    @NotNull
    private LocalTime timeEnd;

    @JsonProperty("max_people_number")
    @NotNull
    private Integer maxPeopleNumber;

    @JsonProperty("platform_instance_id")
    @NotNull
    private Long platformInstanceId;

    @JsonProperty("organizer_tins")
    private List<String> organizerTins;

    @JsonProperty("curator_emails")
    private List<String> curatorEmails;

    @JsonProperty("ticket_types")
    private List<TicketTypeRequest> ticketTypes;
}