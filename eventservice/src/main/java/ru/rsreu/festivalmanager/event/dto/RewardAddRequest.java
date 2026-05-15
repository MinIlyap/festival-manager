package ru.rsreu.festivalmanager.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardAddRequest {
    @NotNull(message = "ID базовой награды (из словаря) обязателен")
    private Long rewardId;

    private String description;
    private Integer place;
    private Integer pointNumber;
}
