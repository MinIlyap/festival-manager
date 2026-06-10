package ru.rsreu.festivalmanager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.user.entity.AppUser;

import java.util.List;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByNickname(String nickname);

    List<AppUser> findByNicknameContainingIgnoreCase(String nickname);
}
