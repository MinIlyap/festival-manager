package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.festival.AgeLimit;

@Repository
public interface AgeLimitRepository extends JpaRepository<AgeLimit, Integer> {
    boolean existsByAgeLimitValue(String ageLimitValue);
}
