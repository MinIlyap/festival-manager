package ru.rsreu.festivalmanager.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.festivalmanager.auth.entity.UserSocialAccount;

import java.util.Optional;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
    Optional<UserSocialAccount> findByProviderAndExternalId(String provider, String externalId);
    Optional<UserSocialAccount> findByUserIdAndProvider(Long userId, String provider);
    long countByUserId(Long userId);
}
