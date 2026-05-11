package ru.rsreu.festivalmanager.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Токен должен быть указан")
    private String refreshToken;
}
