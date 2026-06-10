package ru.rsreu.festivalmanager.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyUpdateRequest {
    @NotBlank(message = "Полное название обязательно")
    @JsonProperty("full_name")
    private String fullName;

    @NotBlank(message = "Название бренда обязательно")
    @JsonProperty("brand_name")
    private String brandName;

    @JsonProperty("website")
    private String website;
}
