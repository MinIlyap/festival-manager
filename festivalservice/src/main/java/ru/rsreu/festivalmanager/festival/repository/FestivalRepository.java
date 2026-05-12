package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.festival.Festival;

import java.util.Optional;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Optional<Festival> findByNameIgnoreCase(String name);
}
