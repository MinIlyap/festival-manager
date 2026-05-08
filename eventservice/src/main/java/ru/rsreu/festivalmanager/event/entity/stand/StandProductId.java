package ru.rsreu.festivalmanager.event.entity.stand;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandProductId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "festival_stand_id", nullable = false)
    private FestivalStand festivalStand;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
