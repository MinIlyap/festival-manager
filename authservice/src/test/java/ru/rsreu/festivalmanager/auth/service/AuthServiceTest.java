package ru.rsreu.festivalmanager.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rsreu.festivalmanager.auth.client.CompanyServiceClient;
import ru.rsreu.festivalmanager.auth.client.UserServiceClient;
import ru.rsreu.festivalmanager.auth.dto.AuthResponse;
import ru.rsreu.festivalmanager.auth.dto.ChangePasswordRequest;
import ru.rsreu.festivalmanager.auth.dto.LoginRequest;
import ru.rsreu.festivalmanager.auth.dto.UserCreationDto;
import ru.rsreu.festivalmanager.auth.dto.UserRegistrationRequest;
import ru.rsreu.festivalmanager.auth.entity.AccessType;
import ru.rsreu.festivalmanager.auth.entity.AuthCredential;
import ru.rsreu.festivalmanager.auth.entity.UserSession;
import ru.rsreu.festivalmanager.auth.entity.UserSocialAccount;
import ru.rsreu.festivalmanager.auth.repository.AuthCredentialRepository;
import ru.rsreu.festivalmanager.auth.repository.UserSessionRepository;
import ru.rsreu.festivalmanager.auth.repository.UserSocialAccountRepository;
import ru.rsreu.festivalmanager.common.util.CryptoUtil;
import ru.rsreu.festivalmanager.jwt.security.JwtProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SYSTEM_TOKEN = "system-token";
    private static final String BEARER_SYSTEM_TOKEN = "Bearer " + SYSTEM_TOKEN;

    @Mock private AuthCredentialRepository credentialRepository;
    @Mock private UserSessionRepository sessionRepository;
    @Mock private UserSocialAccountRepository userSocialAccountRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private CompanyServiceClient companyServiceClient;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CryptoUtil cryptoUtil;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void injectSystemToken() {
        ReflectionTestUtils.setField(authService, "systemToken", SYSTEM_TOKEN);
    }

    private static AuthCredential userCredential() {
        return AuthCredential.builder()
                .id(1L)
                .userId(42L)
                .email("user@example.com")
                .passwordHash("stored-hash")
                .accessType(AccessType.USER)
                .build();
    }

    private static AuthCredential companyCredential() {
        return AuthCredential.builder()
                .id(2L)
                .companyId(777L)
                .email("company@example.com")
                .passwordHash("stored-hash")
                .accessType(AccessType.COMPANY)
                .build();
    }

    /** Makes save() behave like JPA: return what it was given. */
    private void echoSavedCredential() {
        when(credentialRepository.save(any(AuthCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("Регистрация пользователя")
    class RegisterUser {

        private UserRegistrationRequest request() {
            return UserRegistrationRequest.builder()
                    .email("new@example.com")
                    .password("Passw0rd")
                    .nickname("newbie")
                    .birthDate(LocalDate.of(2000, 1, 2))
                    .cityId(5L)
                    .build();
        }

        @Test
        @DisplayName("хранит хеш пароля, а не сам пароль, и выдаёт пару токенов")
        void storesHashedPasswordAndReturnsTokens() {
            when(credentialRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userServiceClient.createUser(eq(BEARER_SYSTEM_TOKEN), any(UserCreationDto.class))).thenReturn(42L);
            when(passwordEncoder.encode("Passw0rd")).thenReturn("hashed");
            when(jwtProvider.generateAccessToken(42L, "new@example.com", "USER")).thenReturn("access-token");
            echoSavedCredential();

            AuthResponse response = authService.registerUser(request());

            ArgumentCaptor<AuthCredential> saved = ArgumentCaptor.forClass(AuthCredential.class);
            verify(credentialRepository).save(saved.capture());
            assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
            assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("Passw0rd");
            assertThat(saved.getValue().getUserId()).isEqualTo(42L);
            assertThat(saved.getValue().getAccessType()).isEqualTo(AccessType.USER);

            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isNotBlank();
        }

        @Test
        @DisplayName("передаёт профиль в userservice ровно из запроса")
        void forwardsProfileToUserService() {
            when(credentialRepository.existsByEmail(anyString())).thenReturn(false);
            when(userServiceClient.createUser(eq(BEARER_SYSTEM_TOKEN), any(UserCreationDto.class))).thenReturn(42L);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(jwtProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("access-token");
            echoSavedCredential();

            authService.registerUser(request());

            ArgumentCaptor<UserCreationDto> dto = ArgumentCaptor.forClass(UserCreationDto.class);
            verify(userServiceClient).createUser(eq(BEARER_SYSTEM_TOKEN), dto.capture());
            assertThat(dto.getValue().getNickname()).isEqualTo("newbie");
            assertThat(dto.getValue().getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 2));
            assertThat(dto.getValue().getCityId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("на занятый email не создаёт профиль в userservice")
        void doesNotCreateUserWhenEmailIsTaken() {
            when(credentialRepository.existsByEmail("new@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerUser(request()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email");

            // иначе в userservice остался бы профиль без учётной записи
            verifyNoInteractions(userServiceClient);
            verify(credentialRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Вход")
    class Login {

        private LoginRequest request(String email, String password) {
            LoginRequest request = new LoginRequest();
            request.setEmail(email);
            request.setPassword(password);
            return request;
        }

        @Test
        @DisplayName("неизвестный email и неверный пароль дают одно и то же сообщение")
        void doesNotRevealWhetherEmailExists() {
            when(credentialRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

            Throwable unknownEmail = org.assertj.core.api.Assertions
                    .catchThrowable(() -> authService.login(request("ghost@example.com", "Passw0rd")));

            AuthCredential credential = userCredential();
            when(credentialRepository.findByEmail("user@example.com")).thenReturn(Optional.of(credential));
            when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

            Throwable wrongPassword = org.assertj.core.api.Assertions
                    .catchThrowable(() -> authService.login(request("user@example.com", "wrong")));

            assertThat(unknownEmail).isInstanceOf(IllegalArgumentException.class);
            assertThat(wrongPassword).isInstanceOf(IllegalArgumentException.class);
            assertThat(wrongPassword.getMessage()).isEqualTo(unknownEmail.getMessage());
        }

        @Test
        @DisplayName("неверный пароль не создаёт сессию")
        void doesNotOpenSessionOnWrongPassword() {
            when(credentialRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userCredential()));
            when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request("user@example.com", "wrong")))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(jwtProvider);
        }

        @Test
        @DisplayName("для компании токен подписывается companyId, а не userId")
        void signsCompanyTokenWithCompanyId() {
            when(credentialRepository.findByEmail("company@example.com")).thenReturn(Optional.of(companyCredential()));
            when(passwordEncoder.matches("Passw0rd", "stored-hash")).thenReturn(true);
            when(jwtProvider.generateAccessToken(777L, "company@example.com", "COMPANY")).thenReturn("company-token");

            AuthResponse response = authService.login(request("company@example.com", "Passw0rd"));

            assertThat(response.getAccessToken()).isEqualTo("company-token");
            verify(jwtProvider).generateAccessToken(777L, "company@example.com", "COMPANY");
        }

        @Test
        @DisplayName("успешный вход открывает сессию на 30 дней")
        void opensSessionForThirtyDays() {
            when(credentialRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userCredential()));
            when(passwordEncoder.matches("Passw0rd", "stored-hash")).thenReturn(true);
            when(jwtProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("access-token");

            AuthResponse response = authService.login(request("user@example.com", "Passw0rd"));

            ArgumentCaptor<UserSession> session = ArgumentCaptor.forClass(UserSession.class);
            verify(sessionRepository).save(session.capture());
            assertThat(session.getValue().getRefreshToken()).isEqualTo(response.getRefreshToken());
            assertThat(session.getValue().getExpiryDate())
                    .isAfter(LocalDateTime.now().plusDays(29))
                    .isBefore(LocalDateTime.now().plusDays(31));
        }
    }

    @Nested
    @DisplayName("Обновление токена")
    class RefreshToken {

        private UserSession session(LocalDateTime expiry) {
            return UserSession.builder()
                    .refreshToken("refresh-token")
                    .user(userCredential())
                    .expiryDate(expiry)
                    .build();
        }

        @Test
        @DisplayName("истёкший токен удаляется и не обменивается")
        void deletesExpiredSession() {
            UserSession expired = session(LocalDateTime.now().minusMinutes(1));
            when(sessionRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("истек");

            verify(sessionRepository).delete(expired);
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("валидный токен одноразовый: старая сессия удаляется, выдаётся новая")
        void rotatesSession() {
            UserSession valid = session(LocalDateTime.now().plusDays(1));
            when(sessionRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(valid));
            when(credentialRepository.findById(1L)).thenReturn(Optional.of(userCredential()));
            when(jwtProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("access-token");

            AuthResponse response = authService.refreshToken("refresh-token");

            verify(sessionRepository).delete(valid);
            ArgumentCaptor<UserSession> created = ArgumentCaptor.forClass(UserSession.class);
            verify(sessionRepository).save(created.capture());
            assertThat(created.getValue().getRefreshToken())
                    .isEqualTo(response.getRefreshToken())
                    .isNotEqualTo("refresh-token");
        }

        @Test
        @DisplayName("неизвестный токен отклоняется")
        void rejectsUnknownToken() {
            when(sessionRepository.findByRefreshToken("nope")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken("nope"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(sessionRepository, never()).delete(any(UserSession.class));
        }
    }

    @Nested
    @DisplayName("Смена пароля")
    class ChangePassword {

        private ChangePasswordRequest request() {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("OldPass1");
            request.setNewPassword("NewPass1");
            return request;
        }

        @Test
        @DisplayName("при неверном старом пароле хеш не меняется")
        void keepsHashWhenOldPasswordIsWrong() {
            when(credentialRepository.findByUserId(42L)).thenReturn(Optional.of(userCredential()));
            when(passwordEncoder.matches("OldPass1", "stored-hash")).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword(42L, "USER", request()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("старый пароль");

            verify(credentialRepository, never()).save(any());
            verify(sessionRepository, never()).deleteAllByUser(any());
        }

        @Test
        @DisplayName("успешная смена разлогинивает все сессии")
        void invalidatesSessionsOnSuccess() {
            AuthCredential credential = userCredential();
            when(credentialRepository.findByUserId(42L)).thenReturn(Optional.of(credential));
            when(passwordEncoder.matches("OldPass1", "stored-hash")).thenReturn(true);
            when(passwordEncoder.encode("NewPass1")).thenReturn("new-hash");

            authService.changePassword(42L, "USER", request());

            assertThat(credential.getPasswordHash()).isEqualTo("new-hash");
            verify(credentialRepository).save(credential);
            // иначе старые refresh-токены продолжили бы работать
            verify(sessionRepository).deleteAllByUser(credential);
        }
    }

    @Nested
    @DisplayName("Назначение роли куратора")
    class AssignCuratorRoles {

        @Test
        @DisplayName("учётные записи компаний не становятся кураторами")
        void skipsCompanyAccounts() {
            AuthCredential user = userCredential();
            AuthCredential company = companyCredential();
            when(credentialRepository.findByUserIdIn(List.of(42L, 777L)))
                    .thenReturn(List.of(user, company));

            authService.assignCuratorRoles(List.of(42L, 777L));

            assertThat(user.getAccessType()).isEqualTo(AccessType.CURATOR);
            assertThat(company.getAccessType()).isEqualTo(AccessType.COMPANY);
            verify(sessionRepository).deleteAllByUser(user);
            verify(sessionRepository, never()).deleteAllByUser(company);
        }

        @Test
        @DisplayName("пустой список не трогает базу")
        void emptyListIsNoop() {
            authService.assignCuratorRoles(List.of());
            authService.assignCuratorRoles(null);

            verifyNoInteractions(credentialRepository, sessionRepository);
        }

        @Test
        @DisplayName("если ни одного пользователя не найдено — ошибка")
        void failsWhenNobodyFound() {
            when(credentialRepository.findByUserIdIn(List.of(99L))).thenReturn(List.of());

            assertThatThrownBy(() -> authService.assignCuratorRoles(List.of(99L)))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(credentialRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Смена email")
    class ChangeEmail {

        @Test
        @DisplayName("занятый email отклоняется до чтения учётной записи")
        void rejectsTakenEmail() {
            when(credentialRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.changeEmail(42L, "USER", "taken@example.com"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(credentialRepository, never()).save(any());
        }

        @Test
        @DisplayName("для компании новый email уезжает в companyservice")
        void propagatesCompanyEmail() {
            when(credentialRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(credentialRepository.findByCompanyId(777L)).thenReturn(Optional.of(companyCredential()));

            authService.changeEmail(777L, "COMPANY", "new@example.com");

            verify(companyServiceClient).updateCompanyEmail(BEARER_SYSTEM_TOKEN, 777L, "new@example.com");
        }

        @Test
        @DisplayName("для пользователя companyservice не вызывается")
        void doesNotTouchCompanyServiceForUser() {
            when(credentialRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(credentialRepository.findByUserId(42L)).thenReturn(Optional.of(userCredential()));

            authService.changeEmail(42L, "USER", "new@example.com");

            verifyNoInteractions(companyServiceClient);
        }
    }

    @Nested
    @DisplayName("Отвязка соцсети")
    class UnlinkSocialAccount {

        @Test
        @DisplayName("нельзя отвязать единственный способ входа без пароля")
        void rejectsUnlinkingLastLoginMethod() {
            AuthCredential noPassword = userCredential();
            noPassword.setPasswordHash(null);
            when(credentialRepository.findByUserId(42L)).thenReturn(Optional.of(noPassword));
            when(userSocialAccountRepository.findByUserIdAndProvider(42L, "yandex"))
                    .thenReturn(Optional.of(new UserSocialAccount()));
            when(userSocialAccountRepository.countByUserId(42L)).thenReturn(1L);

            assertThatThrownBy(() -> authService.unlinkSocialAccount(42L, "yandex"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("единственный способ входа");

            verify(userSocialAccountRepository, never()).delete(any());
        }

        @Test
        @DisplayName("при установленном пароле отвязка проходит")
        void allowsUnlinkWhenPasswordIsSet() {
            UserSocialAccount account = new UserSocialAccount();
            when(credentialRepository.findByUserId(42L)).thenReturn(Optional.of(userCredential()));
            when(userSocialAccountRepository.findByUserIdAndProvider(42L, "yandex"))
                    .thenReturn(Optional.of(account));

            authService.unlinkSocialAccount(42L, "yandex");

            verify(userSocialAccountRepository).delete(account);
        }
    }
}
