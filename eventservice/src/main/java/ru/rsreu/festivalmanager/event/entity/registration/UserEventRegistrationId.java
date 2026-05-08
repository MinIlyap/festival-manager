package ru.rsreu.festivalmanager.event.entity.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRegistrationId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "festival_event_id", nullable = false)
    private FestivalEvent festivalEvent;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
