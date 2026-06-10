package ru.rsreu.festivalmanager.auth.event;

import java.time.OffsetDateTime;

public class AuthEvents {
    public record CompanyAuthDataEvent(Long companyId, String login, String passwordHash,
                                       String comment, OffsetDateTime createdAt) {}
}
