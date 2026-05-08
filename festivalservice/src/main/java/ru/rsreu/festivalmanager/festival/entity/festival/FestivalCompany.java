package ru.rsreu.festivalmanager.festival.entity.festival;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "festival_companies")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FestivalCompany {
    @EmbeddedId
    private FestivalCompanyId festivalCompanyId;
}
