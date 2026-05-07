package ru.rsreu.festivalmanager.festival.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "festival_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FestivalLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "platform_id", nullable = false)
    private Long platformId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "max_people_number", nullable = false)
    private Integer maxPeopleNumber;
}
