package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.stand.StandProduct;
import ru.rsreu.festivalmanager.event.entity.stand.StandProductId;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandProductRepository extends JpaRepository<StandProduct, StandProductId> {
    List<StandProduct> findAllByStandProductId_FestivalStand_Id(Long festivalStandId);
    Optional<StandProduct> findByStandProductId_FestivalStand_IdAndStandProductId_ProductInstance_Id(Long festivalStandId, Long productInstanceId);
}
