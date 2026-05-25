package ru.rsreu.festivalmanager.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.rsreu.festivalmanager.event.entity.stand.StandType;

import java.time.LocalDate;

@Data
@Builder
public class FestivalStandResponse {
    private Long id;
    private Long festivalLocationId;
    private String name;
    private StandType type;
    private String description;
    private Long companyId;
    private LocalDate dateStart;
}
