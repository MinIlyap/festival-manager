package ru.rsreu.festivalmanager.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalCompany;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalCompanyId;
import ru.rsreu.festivalmanager.festival.entity.festival.FestivalLocation;

import java.util.List;

@Repository
public interface FestivalCompanyRepository extends JpaRepository<FestivalCompany, FestivalCompanyId> {
    List<FestivalCompany> findAllByFestivalCompanyId_FestivalLocation(FestivalLocation festivalLocation);

    List<FestivalCompany> findAllByFestivalCompanyId_CompanyId(Long companyId);

    boolean existsByFestivalCompanyId_FestivalLocationIdAndFestivalCompanyId_CompanyId(Long entityId, Long companyId);
}
