package ru.rsreu.festivalmanager.event.entity.stand;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "festival_stands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FestivalStand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "festival_location_id", nullable = false)
    private Long festivalLocationId;

    @ManyToOne
    @JoinColumn(name = "stand_instance_id")
    private StandInstance standInstance;
}
