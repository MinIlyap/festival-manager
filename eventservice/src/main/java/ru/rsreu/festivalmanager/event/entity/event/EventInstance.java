package ru.rsreu.festivalmanager.event.entity.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "event_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private EventType eventType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "approximate_duration")
    private Integer approximateDuration;

    @Column(name = "max_number_people", nullable = false)
    private Integer maxNumberPeople;

    @Column(name = "is_command", nullable = false)
    private Boolean isCommand;

    @Column(name = "max_people_command")
    private Integer maxPeopleCommand;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
