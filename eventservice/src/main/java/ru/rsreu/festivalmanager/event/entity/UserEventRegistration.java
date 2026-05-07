package ru.rsreu.festivalmanager.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_event_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRegistration {
    @EmbeddedId
    private UserEventRegistrationId userEventRegistrationId;

    @ManyToOne
    @JoinColumn(name = "command_id", nullable = false)
    private Command command;
}
