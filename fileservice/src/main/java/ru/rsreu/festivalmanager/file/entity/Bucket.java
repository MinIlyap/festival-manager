package ru.rsreu.festivalmanager.file.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buckets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bucket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
}
