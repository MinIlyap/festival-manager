package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Старый пароль обязателен")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
            message = "Пароль должен содержать от 8 символов, минимум одну заглавную и строчную латинскую букву, а также цифру")
    @JsonProperty("old_password")
    private String oldPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
            message = "Пароль должен содержать от 8 символов, минимум одну заглавную и строчную латинскую букву, а также цифру")
    @JsonProperty("new_password")
    private String newPassword;
}
