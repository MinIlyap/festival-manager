package ru.rsreu.festivalmanager.festival.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "festival_curators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FestivalCurator {
    @EmbeddedId
    private FestivalCuratorId festivalCuratorId;
}
