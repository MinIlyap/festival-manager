package ru.rsreu.festivalmanager.event.entity.stand;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", updatable = true, nullable = false)
    private String name;
}
