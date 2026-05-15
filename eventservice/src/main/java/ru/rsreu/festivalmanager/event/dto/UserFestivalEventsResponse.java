package ru.rsreu.festivalmanager.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFestivalEventsResponse {
    private Long festivalLocationId;
    private List<FestivalEventResponse> events; // Тот самый DTO из Этапа 1
}
