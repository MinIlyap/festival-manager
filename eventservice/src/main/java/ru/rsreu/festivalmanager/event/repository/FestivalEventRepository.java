package ru.rsreu.festivalmanager.event.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.event.EventType;
import ru.rsreu.festivalmanager.event.entity.event.FestivalEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FestivalEventRepository extends JpaRepository<FestivalEvent, Long> {
    @Query("SELECT fe FROM FestivalEvent fe " +
            "JOIN fe.eventInstance ei " +
            "JOIN ei.event e " +
            "WHERE (CAST(:name AS string) IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) " +
            "AND (CAST(:startDate AS date) IS NULL OR ei.dateStart >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR ei.dateStart <= :endDate) " +
            "AND (:locationIds IS NULL OR fe.festivalLocationId IN :locationIds) " +
            "AND (:type IS NULL OR ei.eventType = :type)")
    Page<FestivalEvent> searchWithFilters(
            @Param("name") String name,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("locationIds") List<Long> locationIds,
            @Param("type") EventType type,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fe FROM FestivalEvent fe WHERE fe.id = :id")
    Optional<FestivalEvent> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT fe FROM FestivalEvent fe " +
            "WHERE fe.festivalLocationId = :locationId " +
            "AND (:type IS NULL OR fe.eventInstance.eventType = :type)")
    List<FestivalEvent> findByLocationAndType(
            @Param("locationId") Long locationId,
            @Param("type") EventType type
    );

    List<FestivalEvent> findAllByEventInstance_CompanyId(Long companyId);
}
