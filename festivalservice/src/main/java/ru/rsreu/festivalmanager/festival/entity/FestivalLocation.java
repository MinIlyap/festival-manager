package ru.rsreu.festivalmanager.festival.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
    @EmbeddedId
    private FestivalLocationId festivalLocationId;

    @Column(name = "max_people_number", nullable = false)
    private Integer maxPeopleNumber;
}
