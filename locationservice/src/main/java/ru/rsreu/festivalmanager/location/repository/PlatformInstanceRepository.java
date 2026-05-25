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
    @Query("SELECT p FROM PlatformInstance p WHERE p.platform.id = :platformId AND p.city.id = :cityId AND p.street = :street AND p.house = :house")
    Optional<PlatformInstance> findExistingInstance(
            @Param("platformId") Long platformId,
            @Param("cityId") Long cityId,
            @Param("street") String street,
            @Param("house") String house
    );

    @Query("SELECT p FROM PlatformInstance p WHERE p.city.id = :cityId AND LOWER(p.platform.name) LIKE LOWER(CONCAT('%', :platformName, '%'))")
    Page<PlatformInstance> searchInstancesByCityAndPlatformName(
            @Param("cityId") Long cityId,
            @Param("platformName") String platformName,
            Pageable pageable
    );

    @Query("SELECT p FROM PlatformInstance p WHERE LOWER(p.platform.name) LIKE LOWER(CONCAT('%', :platformName, '%'))")
    Page<PlatformInstance> searchInstancesByPlatformName(
            @Param("platformName") String platformName,
            Pageable pageable
    );

    @Query("SELECT pi.id FROM PlatformInstance pi WHERE pi.city.id = :cityId")
    List<Long> findPlatformInstanceIdsByCityId(@Param("cityId") Long cityId);
}