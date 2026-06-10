package ru.rsreu.festivalmanager.location.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.location.entity.PlatformInstance;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformInstanceRepository extends JpaRepository<PlatformInstance, Long> {
    Optional<PlatformInstance> findByPlatform_IdAndCity_IdAndStreetAndHouse(
            Long platformId, Long cityId, String street, String house);

    Page<PlatformInstance> findByCity_IdAndPlatform_NameContainingIgnoreCase(
            Long cityId, String platformName, Pageable pageable);

    Page<PlatformInstance> findByPlatform_NameContainingIgnoreCase(
            String platformName, Pageable pageable);

    @Query("SELECT pi.id FROM PlatformInstance pi WHERE pi.city.id = :cityId")
    List<Long> findPlatformInstanceIdsByCityId(@Param("cityId") Long cityId);
}