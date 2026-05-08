package ru.rsreu.festivalmanager.event.entity.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "festival_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FestivalEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "festival_location_id", nullable = false)
    private Long festivalLocationId;

    @ManyToOne
    @JoinColumn(name = "event_instance_id", nullable = false)
    private EventInstance eventInstance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
