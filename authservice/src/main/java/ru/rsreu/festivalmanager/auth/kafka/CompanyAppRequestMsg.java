package ru.rsreu.festivalmanager.auth.kafka;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CompanyAppRequestMsg {
    private Long companyId;
    private String companyName;
    private OffsetDateTime createdAt;
}
