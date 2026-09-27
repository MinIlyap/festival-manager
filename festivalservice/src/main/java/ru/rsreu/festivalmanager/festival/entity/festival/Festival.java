package ru.rsreu.festivalmanager.festival.entity.festival;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "festivals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;
}
