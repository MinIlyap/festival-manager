package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FestivalOrganizerDto {
    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("tin")
    private String tin;

    @JsonProperty("brand_name")
    private String brandName;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("email")
    private String email;
}
