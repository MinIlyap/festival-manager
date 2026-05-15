package ru.rsreu.festivalmanager.event.entity.stand;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "festival_stands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalStand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "festival_location_id", nullable = false)
    private Long festivalLocationId;

    @ManyToOne
    @JoinColumn(name = "stand_instance_id")
    private StandInstance standInstance;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
