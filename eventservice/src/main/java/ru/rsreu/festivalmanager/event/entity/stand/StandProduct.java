package ru.rsreu.festivalmanager.event.entity.stand;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stand_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandProduct {
    @EmbeddedId
    private StandProductId standProductId;
}
