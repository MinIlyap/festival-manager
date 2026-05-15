package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.stand.FestivalStand;

@Repository
public interface FestivalStandRepository extends JpaRepository<FestivalStand, Long> {
}
