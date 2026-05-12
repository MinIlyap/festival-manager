package ru.rsreu.festivalmanager.company.repository;

import ru.rsreu.festivalmanager.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByTin(String tin);
    boolean existsByEmail(String email);
}
