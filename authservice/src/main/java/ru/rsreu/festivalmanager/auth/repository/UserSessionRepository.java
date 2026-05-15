package ru.rsreu.festivalmanager.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.auth.entity.AuthCredential;
import ru.rsreu.festivalmanager.auth.entity.UserSession;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    Optional<UserSession> findByRefreshToken(String refreshToken);

    void deleteAllByUser(AuthCredential authCredential);
}
