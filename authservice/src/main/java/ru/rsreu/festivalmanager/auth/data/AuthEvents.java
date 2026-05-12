package ru.rsreu.festivalmanager.auth.data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class AuthEvents {

    public record CompanyAppRequestEvent(Long companyId, String companyName, OffsetDateTime createdAt) {}

    public record CompanyAuthDataEvent(Long companyId, String login, String passwordHash,
                                       String comment, OffsetDateTime createdAt) {}
}
