package ru.rsreu.festivalmanager.event.entity.registration;

import jakarta.persistence.*;
import lombok.*;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "commandMemberId.command", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandMember> members = new ArrayList<>();
}
