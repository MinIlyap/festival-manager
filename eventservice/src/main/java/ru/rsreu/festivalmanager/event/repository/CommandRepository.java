package ru.rsreu.festivalmanager.event.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.registration.Command;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Command c WHERE c.id = :id")
    Optional<Command> findByIdForUpdate(@Param("id") Long id);

    List<Command> findAllByFestivalEvent_Id(Long festivalEventId);
}
