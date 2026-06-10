package ru.rsreu.festivalmanager.festival.dto.festival;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompanyDto {
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
