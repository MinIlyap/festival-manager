package ru.rsreu.festivalmanager.festival.entity.festival;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "age_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgeLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "age_limit_value", unique = true, nullable = false)
    private String ageLimitValue;
}
