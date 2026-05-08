package ru.rsreu.festivalmanager.event.entity.reward;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reward_distributions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardDistribution {
    @EmbeddedId
    private RewardDistributionId rewardDistributionId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
