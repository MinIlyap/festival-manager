package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegistrationRequest {
    @NotBlank(message = "ИНН обязателен")
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен состоять из 10 или 12 цифр")
    @JsonProperty("tin")
    private String tin;

    @NotBlank(message = "Полное название компании обязательно")
    @JsonProperty("full_name")
    private String fullName;

    @NotBlank(message = "Название бренда (Brand Name) обязательно")
    @JsonProperty("brand_name")
    private String brandName;

    @URL
    @JsonProperty("website")
    private String webSite;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @JsonProperty("email")
    private String email;
}
