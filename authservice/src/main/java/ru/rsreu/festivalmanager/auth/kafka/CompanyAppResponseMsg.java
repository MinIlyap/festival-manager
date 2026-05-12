package ru.rsreu.festivalmanager.auth.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class CompanyAppResponseMsg {
    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}