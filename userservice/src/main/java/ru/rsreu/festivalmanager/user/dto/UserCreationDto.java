package ru.rsreu.festivalmanager.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserCreationDto {
    @NotBlank(message = "Никнейм обязателен")
    private String nickname;

    @NotNull(message = "Дата рождения обязательна")
    private LocalDate birthDate;

    @NotNull(message = "ID города обязателен")
    private Long cityId;

    private String accessType;
}
