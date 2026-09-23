package ru.rsreu.festivalmanager.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.auth.client.UserServiceClient;
import ru.rsreu.festivalmanager.auth.dto.UserCreationDto;
import ru.rsreu.festivalmanager.auth.entity.AccessType;
import ru.rsreu.festivalmanager.auth.entity.AuthCredential;
import ru.rsreu.festivalmanager.auth.repository.AuthCredentialRepository;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final AuthCredentialRepository credentialRepository;
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;

    @Value("${internal.system-token}")
    private String systemToken;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Проверка существования админов...");

        createAdminIfNotExists("admin1@festival.com", "AdminOne", "SuperSecurePassword1!");
        createAdminIfNotExists("admin2@festival.com", "AdminTwo", "SuperSecurePassword2!");
    }

    private void createAdminIfNotExists(String email, String nickname, String password) {
        if (!credentialRepository.existsByEmail(email)) {
            try {
                UserCreationDto userDto = UserCreationDto.builder()
                        .nickname(nickname)
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .cityId(1L)
                        .build();

                Long userId = userServiceClient.createUser("Bearer " + systemToken, userDto);

                AuthCredential credential = new AuthCredential();
                credential.setUserId(userId);
                credential.setEmail(email);
                credential.setAccessType(AccessType.ADMIN);
                credential.setPasswordHash(passwordEncoder.encode(password));

                credentialRepository.save(credential);
                log.info("Админ создан: {}", email);

            } catch (Exception e) {
                log.error("Произошла ошибка при создании админа {}: {}", email, e.getMessage());
            }
        }
    }
}
