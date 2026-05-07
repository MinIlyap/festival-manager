package ru.rsreu.festivalmanager.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private EventType eventType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "approximate_duration", nullable = false)
    private Integer approximateDuration;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
