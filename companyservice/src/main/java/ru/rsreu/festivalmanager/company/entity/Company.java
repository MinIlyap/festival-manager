package ru.rsreu.festivalmanager.company.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tin", unique = true, nullable = false)
    private String tin;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "website")
    private String website;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
