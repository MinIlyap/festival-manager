package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Токен должен быть указан")
    @JsonProperty("refresh_token")
    private String refreshToken;
}
