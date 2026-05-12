package ru.rsreu.festivalmanager.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "application_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment")
    private String comment;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "festival_location_id")
    private Long festivalLocationId;
}
