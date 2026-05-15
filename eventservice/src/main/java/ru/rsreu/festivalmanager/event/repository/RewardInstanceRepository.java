package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.reward.RewardInstance;

import java.util.Optional;

@Repository
public interface RewardInstanceRepository extends JpaRepository<RewardInstance, Long> {
    @Query("SELECT ri FROM RewardInstance ri " +
            "WHERE ri.reward.id = :rewardId " +
            "AND (ri.description = :description OR (ri.description IS NULL AND :description IS NULL)) " +
            "AND (ri.place = :place OR (ri.place IS NULL AND :place IS NULL)) " +
            "AND (ri.pointNumber = :pointNumber OR (ri.pointNumber IS NULL AND :pointNumber IS NULL))")
    Optional<RewardInstance> findIdenticalInstance(
            @Param("rewardId") Long rewardId,
            @Param("description") String description,
            @Param("place") Integer place,
            @Param("pointNumber") Integer pointNumber
    );
}
