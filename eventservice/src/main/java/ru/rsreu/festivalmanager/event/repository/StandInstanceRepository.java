package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.stand.StandInstance;
import ru.rsreu.festivalmanager.event.entity.stand.StandType;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StandInstanceRepository extends JpaRepository<StandInstance, Long> {
    @Query("SELECT si FROM StandInstance si " +
            "WHERE si.stand.id = :standId " +
            "AND si.companyId = :companyId " +
            "AND si.standType = :type " +
            "AND si.dateStart = :dateStart " +
            "AND (si.description = :description OR (si.description IS NULL AND :description IS NULL))")
    Optional<StandInstance> findIdenticalInstance(
            @Param("standId") Long standId,
            @Param("companyId") Long companyId,
            @Param("type") StandType type,
            @Param("dateStart") LocalDate dateStart,
            @Param("description") String description
    );
}
