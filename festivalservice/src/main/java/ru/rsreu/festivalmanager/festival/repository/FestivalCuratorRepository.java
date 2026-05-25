package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalCurator;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalCuratorId;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalLocation;

import java.util.List;

@Repository
public interface FestivalCuratorRepository extends JpaRepository<FestivalCurator, FestivalCuratorId> {
    List<FestivalCurator> findAllByFestivalCuratorId_FestivalLocation(FestivalLocation festivalLocation);

    List<FestivalCurator> findAllByFestivalCuratorId_CuratorId(Long curatorId);

    boolean existsByFestivalCuratorId_FestivalLocationIdAndFestivalCuratorId_CuratorId(Long festivalLocationId, Long curatorId);
}