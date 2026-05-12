package ru.rsreu.festivalmanager.application.event;

import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;
import ru.rsreu.festivalmanager.application.entity.ApplicationType;
import java.time.OffsetDateTime;

public class ApplicationEvents {
    public record AppResponseEvent(
            ApplicationType type,
            ApplicationStatus status,
            String comment,
            Long companyId,
            Long festivalLocationId,
            OffsetDateTime updatedAt
    ) {}
}
