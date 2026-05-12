package ru.rsreu.festivalmanager.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CompanyAppRequestMsg {
    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
