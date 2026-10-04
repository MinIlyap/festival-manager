package ru.rsreu.festivalmanager.festival.entity.festival;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "festival_instances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "age_limit_id", nullable = false)
    private AgeLimit ageLimit;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;

    @Column(name = "max_people_number", nullable = false)
    private Integer maxPeopleNumber;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
}
