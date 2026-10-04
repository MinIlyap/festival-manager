package ru.rsreu.festivalmanager.festival.entity.ticket;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalLocation;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_type_instances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "festival_location_id", nullable = false)
    private FestivalLocation festivalLocation;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;
}
