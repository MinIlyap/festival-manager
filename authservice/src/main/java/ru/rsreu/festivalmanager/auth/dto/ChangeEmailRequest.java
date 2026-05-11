package ru.rsreu.festivalmanager.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmailRequest {
    @NotBlank(message = "Email обязателен")
    @Email
    private String newEmail;
}
