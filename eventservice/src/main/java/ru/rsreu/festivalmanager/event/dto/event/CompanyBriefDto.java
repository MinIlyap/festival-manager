package ru.rsreu.festivalmanager.event.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompanyBriefDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("brand_name")
    private String brandName;

    @JsonProperty("full_name")
    private String fullName;
}
