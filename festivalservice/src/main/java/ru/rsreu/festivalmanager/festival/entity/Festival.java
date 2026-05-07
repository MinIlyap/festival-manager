package ru.rsreu.festivalmanager.festival.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "festivals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "age_limit_id", nullable = false)
    private AgeLimit ageLimit;

    @Column(name = "description")
    private String description;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
