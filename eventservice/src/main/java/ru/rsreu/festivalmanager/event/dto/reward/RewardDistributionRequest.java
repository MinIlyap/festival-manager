package ru.rsreu.festivalmanager.event.dto.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardDistributionRequest {
    @NotNull(message = "Список пользователей не может быть пустым")
    @JsonProperty("user_ids")
    private List<Long> userIds;
}
