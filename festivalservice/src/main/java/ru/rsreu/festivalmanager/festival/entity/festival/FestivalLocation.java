package ru.rsreu.festivalmanager.festival.entity.festival;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "festival_locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "festival_instance_id", nullable = false)
    private FestivalInstance festivalInstance;

    @Column(name = "platform_instance_id", nullable = false)
    private Long platformInstanceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
