package ru.rsreu.festivalmanager.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "commands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Command {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "event_organization_id", nullable = false)
    private EventOrganization eventOrganization;

    @Column(name = "creator_id")
    private Long creatorId;
}
