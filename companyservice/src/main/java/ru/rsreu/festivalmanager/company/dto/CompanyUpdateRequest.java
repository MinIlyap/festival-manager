package ru.rsreu.festivalmanager.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyUpdateRequest {

    @JsonProperty("full_name")
    @NotBlank(message = "Полное название обязательно")
    private String fullName;

    @JsonProperty("brand_name")
    @NotBlank(message = "Название бренда обязательно")
    private String brandName;

    @JsonProperty("website")
    private String website;
}
