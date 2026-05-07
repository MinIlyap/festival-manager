package ru.rsreu.festivalmanager.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRegistrationId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "event_organization_id", nullable = false)
    private EventOrganization eventOrganization;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
