package ru.rsreu.festivalmanager.festival.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalInstance;

import java.util.List;
import java.util.Optional;

@Repository
public interface FestivalInstanceRepository extends JpaRepository<FestivalInstance, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FestivalInstance f WHERE f.id = :id")
    Optional<FestivalInstance> findByIdWithPessimisticLock(@Param("id") Long id);

    List<FestivalInstance> findByFestivalIdOrderByDateStartDesc(Long festivalId);
}
