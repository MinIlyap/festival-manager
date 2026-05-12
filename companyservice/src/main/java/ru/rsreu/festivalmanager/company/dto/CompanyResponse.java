package ru.rsreu.festivalmanager.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("tin")
    private String tin;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("brand_name")
    private String brandName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("website")
    private String website;

    @JsonProperty("is_active")
    private boolean isActive;
}