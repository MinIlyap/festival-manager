package ru.rsreu.festivalmanager.event.dto.stand;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rsreu.festivalmanager.event.entity.stand.StandType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalStandCreateRequest {
    @NotNull(message = "ID базового стенда обязательно")
    @JsonProperty("stand_id")
    private Long standId;

    @NotNull(message = "ID локации фестиваля обязательно")
    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @NotNull(message = "Тип стенда обязателен")
    @JsonProperty("type")
    private StandType type;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Дата начала обязательна")
    @JsonProperty("date_start")
    private LocalDate dateStart;

    @JsonProperty("organizer_company_id")
    private Long organizerCompanyId;
}
