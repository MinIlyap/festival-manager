package ru.rsreu.festivalmanager.festival.entity.ticket;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "ticket_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;
}
