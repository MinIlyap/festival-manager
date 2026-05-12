package ru.rsreu.festivalmanager.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.location.entity.City;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);

    List<City> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
