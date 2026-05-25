package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalLocation;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalLocationRepository extends JpaRepository<FestivalLocation, Long> {
    List<FestivalLocation> findByFestivalInstanceId(Long festivalInstanceId);

    @Query("SELECT fl FROM FestivalLocation fl " +
            "WHERE fl.festivalInstance.isVisible = true " +
            "AND (CAST(:name AS string) IS NULL OR LOWER(fl.festivalInstance.festival.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) " +
            "AND (CAST(:startDate AS date) IS NULL OR fl.festivalInstance.dateStart >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR fl.festivalInstance.dateEnd <= :endDate)")
    List<FestivalLocation> searchActiveFestivals(
            @Param("name") String name,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<FestivalLocation> findAllByPlatformInstanceIdIn(List<Long> platformInstanceIds);
}
