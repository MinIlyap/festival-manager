package ru.rsreu.festivalmanager.auth.kafka;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CompanyAuthMsg {
    private Long companyId;
    private String login;
    private String passwordHash;
    private String comment;
    private OffsetDateTime createdAt;
}
