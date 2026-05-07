package ru.rsreu.festivalmanager.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "festival_location_id", nullable = false)
    private Long festivalLocationId;

    @Column(name = "max_number_people", nullable = false)
    private Integer maxNumberPeople;

    @Column(name = "is_command", nullable = false)
    private Boolean isCommand;

    @Column(name = "max_people_command")
    private Integer maxPeopleCommand;
}
