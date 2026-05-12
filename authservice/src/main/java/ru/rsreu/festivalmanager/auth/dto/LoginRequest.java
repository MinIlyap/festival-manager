package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @JsonProperty("password")
    private String password;
}
