package ru.rsreu.festivalmanager.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CompanyCreateRequest {
    @NotBlank(message = "ИНН обязателен")
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен состоять из 10 или 12 цифр")
    @JsonProperty("tin")
    private String tin;

    @NotBlank(message = "Полное название обязательно")
    @JsonProperty("full_name")
    private String fullName;

    @NotBlank(message = "Название бренда обязательно")
    @JsonProperty("brand_name")
    private String brandName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @JsonProperty("email")
    private String email;

    @JsonProperty("website")
    private String website;
}
