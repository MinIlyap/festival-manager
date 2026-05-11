package ru.rsreu.festivalmanager.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.auth.entity.AuthCredential;

import java.util.Optional;

@Repository
public interface AuthCredentialRepository extends JpaRepository<AuthCredential, Long> {

    boolean existsByEmail(String email);

    Optional<AuthCredential> findByEmail(String email);

    Optional<AuthCredential> findByUserId(Long userId);

    Optional<AuthCredential> findByCompanyId(Long companyId);
}
