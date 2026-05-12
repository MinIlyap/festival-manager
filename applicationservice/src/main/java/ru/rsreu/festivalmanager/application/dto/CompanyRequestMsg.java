package ru.rsreu.festivalmanager.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CompanyRequestMsg {
    @JsonProperty("company_id")
    private Long companyId;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
