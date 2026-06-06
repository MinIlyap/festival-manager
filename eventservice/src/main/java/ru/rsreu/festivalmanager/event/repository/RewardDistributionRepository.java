package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.reward.RewardDistribution;
import ru.rsreu.festivalmanager.event.entity.reward.RewardDistributionId;

import java.util.List;

@Repository
public interface RewardDistributionRepository extends JpaRepository<RewardDistribution, RewardDistributionId> {
    boolean existsByRewardDistributionId_EventReward_IdAndRewardDistributionId_UserId(Long eventRewardId, Long userId);

    List<RewardDistribution> findAllByRewardDistributionId_UserId(Long userId);
}
