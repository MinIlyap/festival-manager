package ru.rsreu.festivalmanager.event.entity.reward;

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
public class RewardDistributionId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "event_reward_id", nullable = false)
    private EventReward eventReward;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
