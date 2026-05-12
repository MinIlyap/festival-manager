package ru.rsreu.festivalmanager.auth.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CompanyAuthMsg {
    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("login")
    private String login;

    @JsonProperty("password_hash")
    private String passwordHash;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
