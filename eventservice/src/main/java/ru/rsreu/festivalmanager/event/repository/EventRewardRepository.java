package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.reward.EventReward;

import java.util.List;

@Repository
public interface EventRewardRepository extends JpaRepository<EventReward, Long> {
    List<EventReward> findAllByFestivalEvent_Id(Long festivalEventId);
}
