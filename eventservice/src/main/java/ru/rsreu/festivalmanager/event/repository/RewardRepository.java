package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.reward.Reward;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
}
