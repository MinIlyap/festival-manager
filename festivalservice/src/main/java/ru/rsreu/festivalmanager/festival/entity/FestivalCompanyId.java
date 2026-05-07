package ru.rsreu.festivalmanager.festival.entity;

import jakarta.persistence.Column;
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
public class FestivalCompanyId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "company_id", nullable = false)
    private Long companyId;
}
