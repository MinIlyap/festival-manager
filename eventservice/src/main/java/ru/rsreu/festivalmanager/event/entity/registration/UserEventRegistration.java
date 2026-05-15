package ru.rsreu.festivalmanager.event.entity.registration;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_event_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRegistration {
    @EmbeddedId
    private UserEventRegistrationId userEventRegistrationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
