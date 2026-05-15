package ru.rsreu.festivalmanager.event.dto;

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
    private List<Long> userIds;
}
