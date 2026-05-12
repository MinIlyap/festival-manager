package ru.rsreu.festivalmanager.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.application.entity.Application;
import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;
import ru.rsreu.festivalmanager.application.entity.ApplicationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a WHERE a.data.companyId = :companyId")
    Optional<Application> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT a FROM Application a WHERE a.data.festivalLocationId = :festivalLocationId")
    Optional<Application> findByFestivalLocationId(@Param("festivalLocationId") Long festivalLocationId);

    List<Application> findAllBySenderIdOrderByUpdatedAtDesc(Long senderId);
    
    @Query("SELECT a FROM Application a WHERE " +
            "(:type IS NULL OR a.applicationType = :type) AND " +
            "(:status IS NULL OR a.applicationStatus = :status) " +
            "ORDER BY a.updatedAt DESC")
    List<Application> findAllWithFilters(
            @Param("type") ApplicationType type,
            @Param("status") ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.agentId = :agentId AND " +
            "(:type IS NULL OR a.applicationType = :type) AND " +
            "(:status IS NULL OR a.applicationStatus = :status) " +
            "ORDER BY a.updatedAt DESC")
    List<Application> findAdminHistory(
            @Param("agentId") Long agentId,
            @Param("type") ApplicationType type,
            @Param("status") ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.senderId = :senderId " +
            "AND (:status IS NULL OR a.applicationStatus = :status) " +
            "ORDER BY a.updatedAt DESC")
    List<Application> findAllBySenderIdWithFilters(
            @Param("senderId") Long senderId,
            @Param("status") ApplicationStatus status);
}
