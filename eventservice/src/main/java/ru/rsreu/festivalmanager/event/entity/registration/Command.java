package ru.rsreu.festivalmanager.event.entity.registration;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;

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
    @JoinColumn(name = "festival_event_id", nullable = false)
    private FestivalEvent festivalEvent;

    @Column(name = "creator_id")
    private Long creatorId;
}
