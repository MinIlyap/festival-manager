package ru.rsreu.festivalmanager.festival.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "age_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgeLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "age_limit_value", unique = true, nullable = false)
    private String ageLimitValue;
}
