package ru.rsreu.festivalmanager.festival.entity.festival;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "festival_curators")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FestivalCurator {
    @EmbeddedId
    private FestivalCuratorId festivalCuratorId;
}
