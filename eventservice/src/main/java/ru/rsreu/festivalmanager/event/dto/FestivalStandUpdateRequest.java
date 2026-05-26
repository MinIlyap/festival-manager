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
public class FestivalStandUpdateRequest {
    @NotNull(message = "Тип стенда обязателен")
    private StandType type;

    private String description;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate dateStart;

    private Long organizerCompanyId;
}
