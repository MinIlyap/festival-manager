package ru.rsreu.festivalmanager.event.entity.reward;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;

@Entity
@Table(name = "event_rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "festival_event_id", nullable = false)
    private FestivalEvent festivalEvent;

    @ManyToOne
    @JoinColumn(name = "reward_instance_id", nullable = false)
    private RewardInstance rewardInstance;
}
