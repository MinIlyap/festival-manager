package ru.rsreu.festivalmanager.event.entity.stand;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "stand_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private StandType standType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @ManyToOne
    @JoinColumn(name = "stand_id", nullable = false)
    private Stand stand;
}
