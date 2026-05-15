package ru.rsreu.festivalmanager.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserCreationDto {
    @NotBlank(message = "Никнейм обязателен")
    @JsonProperty("nickname")
    private String nickname;

    @NotNull(message = "Дата рождения обязательна")
    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @NotNull(message = "ID города обязателен")
    @JsonProperty("city_id")
    private Long cityId;
}
