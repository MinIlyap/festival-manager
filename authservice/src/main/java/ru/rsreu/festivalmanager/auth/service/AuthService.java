package ru.rsreu.festivalmanager.auth.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.auth.client.CompanyServiceClient;
import ru.rsreu.festivalmanager.auth.client.UserServiceClient;
import ru.rsreu.festivalmanager.auth.data.AuthEvents;
import ru.rsreu.festivalmanager.auth.dto.*;
import ru.rsreu.festivalmanager.auth.entity.AuthCredential;
import ru.rsreu.festivalmanager.auth.entity.UserSession;
import ru.rsreu.festivalmanager.auth.repository.AuthCredentialRepository;
import ru.rsreu.festivalmanager.auth.repository.UserSessionRepository;
import ru.rsreu.festivalmanager.jwt.security.JwtProvider;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthCredentialRepository credentialRepository;
    private final UserSessionRepository sessionRepository;
    private final UserServiceClient userServiceClient;
    private final CompanyServiceClient companyServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getBearerSystemToken() {
        return "Bearer " + systemToken;
    }

    @Transactional
    public AuthResponse registerUser(UserRegistrationRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }

        UserCreationDto userDto = new UserCreationDto(request.getNickname(), request.getBirthDate(),
                request.getCityId(), "USER");
        Long userId = userServiceClient.createUser(getBearerSystemToken(), userDto);

        AuthCredential credential = new AuthCredential();
        credential.setUserId(userId);
        credential.setEmail(request.getEmail());
        credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        AuthCredential savedCredential = credentialRepository.save(credential);

        return generateTokens(savedCredential, "USER");
    }

    @Transactional
    public void submitCompanyApplication(CompanyRegistrationRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }

        CompanyRegistrationRequest companyDto = new CompanyRegistrationRequest(
                request.getTin(), request.getFullName(), request.getBrandName(), request.getEmail(), request.getWebSite()
        );
        Long companyId = companyServiceClient.createInactiveCompany(getBearerSystemToken(), companyDto);
    }

    @Transactional
    public void processCompanyApplicationResponse(Long companyId, String status, String comment) {
        CompanyInfoDto companyInfo = companyServiceClient.getCompanyById(getBearerSystemToken(), companyId);
        String email = companyInfo.getEmail();

        if ("APPROVED".equalsIgnoreCase(status)) {
            companyServiceClient.activateCompany(getBearerSystemToken(), companyId);

            String rawPassword = RandomStringUtils.random(8, true, true);

            AuthCredential credential = new AuthCredential();
            credential.setCompanyId(companyId);
            credential.setEmail(email);
            String encodedPassword = passwordEncoder.encode(rawPassword);
            credential.setPasswordHash(encodedPassword);
            credentialRepository.save(credential);

            eventPublisher.publishEvent(
                    new AuthEvents.CompanyAuthDataEvent(companyId, email, encodedPassword,
                            comment, OffsetDateTime.now())
            );
        } else {
            eventPublisher.publishEvent(
                    new AuthEvents.CompanyAuthDataEvent(companyId, email, null,
                            comment, OffsetDateTime.now())
            );
        }
    }

    public AuthCredential getCurrentUserInfo(Long id, String role) {
        if ("COMPANY".equals(role)) {
            return credentialRepository.findByCompanyId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Компания не найдена"));
        } else {
            return credentialRepository.findByUserId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        }
    }

    public AuthCredential getUserByEmail(String email) {

        return credentialRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Transactional
    public void changeEmail(Long id, String role, String newEmail) {
        if (credentialRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Данный Email уже занят");
        }
        AuthCredential credential = getCurrentUserInfo(id, role);
        credential.setEmail(newEmail);
        credentialRepository.save(credential);
    }

    @Transactional
    public void changePassword(Long id, String role, ChangePasswordRequest request) {
        AuthCredential credential = getCurrentUserInfo(id, role);

        if (!passwordEncoder.matches(request.getOldPassword(), credential.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный старый пароль");
        }

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credentialRepository.save(credential);

        sessionRepository.deleteByUser_UserId(credential.getUserId());
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Невалидный refresh token"));

        if (session.getExpiryDate().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            throw new IllegalArgumentException("Refresh token истек");
        }

        AuthCredential credential = credentialRepository.findByUserId(session.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        sessionRepository.delete(session);
        return generateTokens(credential, extractRole(credential));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AuthCredential credential = credentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }

        return generateTokens(credential, extractRole(credential));
    }

    private AuthResponse generateTokens(AuthCredential credential, String role) {
        Long subjectId = credential.getUserId();

        String accessToken = jwtProvider.generateAccessToken(subjectId, credential.getEmail(), role);
        String refreshToken = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setRefreshToken(refreshToken);
        session.setUser(credential);
        session.setExpiryDate(LocalDateTime.now().plusDays(30));
        sessionRepository.save(session);

        return new AuthResponse(accessToken, refreshToken);
    }

    private String extractRole(AuthCredential credential) {
        if (credential.getCompanyId() != null) {
            return "COMPANY";
        }

        return userServiceClient.getUserRole(getBearerSystemToken(), credential.getUserId());
    }
}
