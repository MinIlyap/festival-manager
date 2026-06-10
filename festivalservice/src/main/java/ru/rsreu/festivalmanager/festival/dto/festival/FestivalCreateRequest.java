package ru.rsreu.festivalmanager.festival.dto.festival;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.rsreu.festivalmanager.festival.dto.ticket.TicketTypeRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class FestivalCreateRequest {
    @NotBlank(message = "Название фестиваля обязательно")
    @JsonProperty("festival_name")
    private String festivalName;

    @NotNull(message = "Возрастное ограничение обязательно")
    @JsonProperty("age_limit_id")
    private Integer ageLimitId;

    @JsonProperty("description")
    private String description;

    @NotNull
    @JsonProperty("date_start")
    private LocalDate dateStart;

    @NotNull
    @JsonProperty("time_start")
    private LocalTime timeStart;

    @NotNull
    @JsonProperty("date_end")
    private LocalDate dateEnd;

    @NotNull
    @JsonProperty("time_end")
    private LocalTime timeEnd;

    @NotNull
    @JsonProperty("max_people_number")
    private Integer maxPeopleNumber;

    @NotNull
    @JsonProperty("platform_instance_id")
    private Long platformInstanceId;

    @JsonProperty("organizer_tins")
    private List<String> organizerTins;

    @JsonProperty("curator_emails")
    private List<String> curatorEmails;

    @JsonProperty("ticket_types")
    private List<TicketTypeRequest> ticketTypes;
}
