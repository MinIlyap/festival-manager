package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.stand.FestivalStand;
import ru.rsreu.festivalmanager.event.entity.stand.StandType;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalStandRepository extends JpaRepository<FestivalStand, Long> {
    List<FestivalStand> findAllByFestivalLocationId(Long festivalLocationId);

    @Query("SELECT fs FROM FestivalStand fs " +
            "JOIN fs.standInstance si " +
            "JOIN si.stand s " +
            "WHERE (CAST(:name AS string) IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) " +
            "AND (:locationIds IS NULL OR fs.festivalLocationId IN :locationIds) " +
            "AND (:type IS NULL OR si.standType = :type) " +
            "AND (CAST(:dateStart AS date) IS NULL OR si.dateStart >= :dateStart)")
    Page<FestivalStand> searchStands(
            @Param("name") String name,
            @Param("locationIds") List<Long> locationIds,
            @Param("type") StandType type,
            @Param("dateStart") LocalDate dateStart,
            Pageable pageable
    );
}
