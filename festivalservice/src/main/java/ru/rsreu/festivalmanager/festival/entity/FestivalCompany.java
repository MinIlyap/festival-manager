package ru.rsreu.festivalmanager.festival.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "festival_companies")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FestivalCompany {
    @EmbeddedId
    private FestivalCompanyId festivalCompanyId;
}
