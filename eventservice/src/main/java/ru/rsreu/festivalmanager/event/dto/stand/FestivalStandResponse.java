package ru.rsreu.festivalmanager.event.dto.stand;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.rsreu.festivalmanager.event.entity.stand.StandType;

import java.time.LocalDate;

@Data
@Builder
public class FestivalStandResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private StandType type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("organizer_company_id")
    private Long organizerCompanyId;

    @JsonProperty("date_start")
    private LocalDate dateStart;
}
