package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmailRequest {
    @NotBlank(message = "Email обязателен")
    @Email
    @JsonProperty("new_email")
    private String newEmail;
}
