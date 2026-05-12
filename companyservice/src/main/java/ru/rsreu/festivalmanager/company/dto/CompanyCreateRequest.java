package ru.rsreu.festivalmanager.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CompanyCreateRequest {

    @JsonProperty("tin")
    @NotBlank(message = "ИНН обязателен")
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен состоять из 10 или 12 цифр")
    private String tin;

    @JsonProperty("full_name")
    @NotBlank(message = "Полное название обязательно")
    private String fullName;

    @JsonProperty("brand_name")
    @NotBlank(message = "Название бренда обязательно")
    private String brandName;

    @JsonProperty("email")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @JsonProperty("website")
    private String website;
}