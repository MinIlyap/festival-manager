package ru.rsreu.festivalmanager.event.dto.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
    @JsonProperty("reward_id")
    private Long rewardId;

    @NotBlank(message = "Описание награды обязательно")
    @JsonProperty("description")
    private String description;

    @JsonProperty("place")
    private Integer place;

    @JsonProperty("point_number")
    private Integer pointNumber;
}
