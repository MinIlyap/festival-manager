package ru.rsreu.festivalmanager.event.entity.reward;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reward_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "place")
    private Integer place;

    @Column(name = "point_number")
    private Integer pointNumber;

    @ManyToOne
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;
}
