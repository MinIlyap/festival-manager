package ru.rsreu.festivalmanager.event.dto;

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
    private Long standId;

    @NotNull(message = "ID локации фестиваля обязательно")
    private Long festivalLocationId;

    @NotNull(message = "Тип стенда обязателен")
    private StandType type;

    private String description;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate dateStart;

    private Long organizerCompanyId;
}
