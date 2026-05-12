package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCreateDto {
    @JsonProperty("tin")
    private String tin;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("brand_name")
    private String brandName;

    @JsonProperty("email")
    private String email;
}