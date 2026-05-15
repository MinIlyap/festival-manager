package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.stand.StandProduct;
import ru.rsreu.festivalmanager.event.entity.stand.StandProductId;

@Repository
public interface StandProductRepository extends JpaRepository<StandProduct, StandProductId> {
}
