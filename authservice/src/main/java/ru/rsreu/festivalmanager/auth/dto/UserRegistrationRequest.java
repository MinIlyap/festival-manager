package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный формат email")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Пароль не должен быть пустым")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
            message = "Пароль должен содержать от 8 символов, минимум одну заглавную и строчную латинскую букву, а также цифру")
    @JsonProperty("password")
    private String password;

    @NotBlank(message = "Никнейм не должен быть пустым")
    @Size(min = 3, max = 70, message = "Никнейм должен быть от 3 до 70 символов")
    @JsonProperty("nickname")
    private String nickname;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @NotNull(message = "Город обязателен")
    @JsonProperty("city_id")
    private Long cityId;
}