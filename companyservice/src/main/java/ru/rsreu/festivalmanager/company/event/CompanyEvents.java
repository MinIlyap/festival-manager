package ru.rsreu.festivalmanager.company.event;

import java.time.OffsetDateTime;

public class CompanyEvents {
    public record CompanyAppRequestEvent(Long companyId, String companyName, OffsetDateTime createdAt) {}
}
