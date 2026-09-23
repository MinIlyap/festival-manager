package ru.rsreu.festivalmanager.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rsreu.festivalmanager.common.util.CryptoUtil;
import ru.rsreu.festivalmanager.user.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.user.client.LocationServiceClient;
import ru.rsreu.festivalmanager.user.dto.BlackListAddRequest;
import ru.rsreu.festivalmanager.user.dto.BlacklistStatusResponse;
import ru.rsreu.festivalmanager.user.dto.CityNameResponse;
import ru.rsreu.festivalmanager.user.dto.UserCreationDto;
import ru.rsreu.festivalmanager.user.dto.UserInfoPatchRequest;
import ru.rsreu.festivalmanager.user.entity.AppUser;
import ru.rsreu.festivalmanager.user.entity.BlackList;
import ru.rsreu.festivalmanager.user.mapper.UserMapper;
import ru.rsreu.festivalmanager.user.mapper.UserMapperImpl;
import ru.rsreu.festivalmanager.user.repository.AppUserRepository;
import ru.rsreu.festivalmanager.user.repository.BlackListRepository;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String BEARER = "Bearer system-token";

    @Mock private AppUserRepository userRepository;
    @Mock private BlackListRepository blackListRepository;
    @Mock private LocationServiceClient locationClient;
    @Mock private FestivalServiceClient festivalClient;
    @Mock private CryptoUtil cryptoUtil;

    private final UserMapper userMapper = new UserMapperImpl();
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, userMapper, blackListRepository,
                locationClient, festivalClient, cryptoUtil);
        ReflectionTestUtils.setField(service, "systemToken", "system-token");
    }

    private static AppUser user() {
        return AppUser.builder()
                .id(42L)
                .nickname("tester")
                .birthDate(LocalDate.of(1999, 3, 10))
                .city_id(7L)
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .phoneNumber("+79990000000")
                .passportSeriaHash("enc-seria")
                .passportNumberHash("enc-number")
                .build();
    }

    @Nested
    @DisplayName("Создание пользователя")
    class CreateUser {

        private UserCreationDto dto() {
            UserCreationDto dto = new UserCreationDto();
            dto.setNickname("newbie");
            dto.setBirthDate(LocalDate.of(2000, 1, 2));
            dto.setCityId(7L);
            return dto;
        }

        @Test
        @DisplayName("несуществующий город отклоняется до сохранения")
        void rejectsUnknownCity() {
            when(locationClient.existsCityById(BEARER, 7L)).thenReturn(false);

            assertThatThrownBy(() -> service.createUser(dto()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("город");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("профиль сохраняется с данными из запроса")
        void savesProfile() {
            when(locationClient.existsCityById(BEARER, 7L)).thenReturn(true);
            when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
                AppUser saved = invocation.getArgument(0);
                saved.setId(42L);
                return saved;
            });

            Long id = service.createUser(dto());

            ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
            verify(userRepository).save(saved.capture());
            assertThat(saved.getValue().getNickname()).isEqualTo("newbie");
            assertThat(saved.getValue().getCity_id()).isEqualTo(7L);
            assertThat(id).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("Частичное обновление (PATCH)")
    class PatchUser {

        @Test
        @DisplayName("null-поля не затирают существующие значения")
        void leavesNullFieldsUntouched() {
            AppUser existing = user();
            when(userRepository.findById(42L)).thenReturn(Optional.of(existing));

            UserInfoPatchRequest request = new UserInfoPatchRequest();
            request.setFirstName("Пётр");

            service.patchUser(42L, request);

            assertThat(existing.getFirstName()).isEqualTo("Пётр");
            assertThat(existing.getLastName()).isEqualTo("Иванов");
            assertThat(existing.getMiddleName()).isEqualTo("Иванович");
            assertThat(existing.getPhoneNumber()).isEqualTo("+79990000000");
            assertThat(existing.getCity_id()).isEqualTo(7L);
            assertThat(existing.getPassportSeriaHash()).isEqualTo("enc-seria");
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("паспорт сохраняется только в зашифрованном виде")
        void encryptsPassportData() {
            AppUser existing = user();
            when(userRepository.findById(42L)).thenReturn(Optional.of(existing));
            when(cryptoUtil.encrypt("4600")).thenReturn("enc-4600");
            when(cryptoUtil.encrypt("123456")).thenReturn("enc-123456");

            UserInfoPatchRequest request = new UserInfoPatchRequest();
            request.setPassportSeries("4600");
            request.setPassportNumber("123456");

            service.patchUser(42L, request);

            assertThat(existing.getPassportSeriaHash()).isEqualTo("enc-4600");
            assertThat(existing.getPassportNumberHash()).isEqualTo("enc-123456");
            assertThat(existing.getPassportSeriaHash()).isNotEqualTo("4600");
            assertThat(existing.getPassportNumberHash()).isNotEqualTo("123456");
        }

        @Test
        @DisplayName("телефон из чёрного списка не принимается")
        void rejectsBlacklistedPhone() {
            when(userRepository.findById(42L)).thenReturn(Optional.of(user()));
            when(blackListRepository.isPhoneInBlackList(eq("+79990000000"), any(LocalDateTime.class)))
                    .thenReturn(true);

            UserInfoPatchRequest request = new UserInfoPatchRequest();
            request.setPhoneNumber("+79990000000");

            assertThatThrownBy(() -> service.patchUser(42L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("заблокированному аккаунту");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("паспорт из чёрного списка не принимается")
        void rejectsBlacklistedPassport() {
            when(userRepository.findById(42L)).thenReturn(Optional.of(user()));
            when(cryptoUtil.encrypt("4600")).thenReturn("enc-4600");
            when(cryptoUtil.encrypt("123456")).thenReturn("enc-123456");
            when(blackListRepository.isPassportInBlackList(eq("enc-4600"), eq("enc-123456"), any(LocalDateTime.class)))
                    .thenReturn(true);

            UserInfoPatchRequest request = new UserInfoPatchRequest();
            request.setPassportSeries("4600");
            request.setPassportNumber("123456");

            assertThatThrownBy(() -> service.patchUser(42L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("паспортные данные");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("смена города проверяется в locationservice")
        void validatesNewCity() {
            when(userRepository.findById(42L)).thenReturn(Optional.of(user()));
            when(locationClient.existsCityById(BEARER, 99L)).thenReturn(false);

            UserInfoPatchRequest request = new UserInfoPatchRequest();
            request.setCityId(99L);

            assertThatThrownBy(() -> service.patchUser(42L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("город");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Чтение профиля")
    class ReadProfile {

        @Test
        @DisplayName("getMe расшифровывает паспорт и подставляет название города")
        void decryptsPassportAndResolvesCity() {
            when(userRepository.findById(42L)).thenReturn(Optional.of(user()));
            when(cryptoUtil.decrypt("enc-seria")).thenReturn("4600");
            when(cryptoUtil.decrypt("enc-number")).thenReturn("123456");
            CityNameResponse city = new CityNameResponse();
            city.setName("Рязань");
            when(locationClient.getCityById(BEARER, 7L)).thenReturn(city);

            var response = service.getMe(42L);

            assertThat(response.getPassportSeries()).isEqualTo("4600");
            assertThat(response.getPassportNumber()).isEqualTo("123456");
            assertThat(response.getCityName()).isEqualTo("Рязань");
            assertThat(response.getNickname()).isEqualTo("tester");
        }

        @Test
        @DisplayName("недоступность locationservice не ломает выдачу профиля")
        void survivesLocationServiceFailure() {
            when(userRepository.findById(42L)).thenReturn(Optional.of(user()));
            when(locationClient.getCityById(BEARER, 7L)).thenThrow(new RuntimeException("location down"));

            var response = service.getMe(42L);

            assertThat(response.getCityName()).isNull();
            assertThat(response.getCityId()).isEqualTo(7L);
            assertThat(response.getNickname()).isEqualTo("tester");
        }

        @Test
        @DisplayName("поиск по никнейму отдаёт не больше десяти совпадений")
        void limitsSearchResults() {
            List<AppUser> many = java.util.stream.IntStream.rangeClosed(1, 25)
                    .mapToObj(i -> AppUser.builder().id((long) i).nickname("user" + i).build())
                    .toList();
            when(userRepository.findByNicknameContainingIgnoreCase("user")).thenReturn(many);

            assertThat(service.searchUsersByNickname("user")).hasSize(10);
        }
    }

    @Nested
    @DisplayName("Чёрный список")
    class Blacklist {

        @Test
        @DisplayName("куратор без доступа к пользователю не может его заблокировать")
        void rejectsCuratorWithoutAccess() {
            when(festivalClient.validateCuratorAccessToUser(BEARER, 1L, 42L)).thenReturn(false);

            BlackListAddRequest request = new BlackListAddRequest();
            request.setReason("нарушение");
            request.setDays(7);

            assertThatThrownBy(() -> service.addUserToBlackList(1L, 42L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("не можете заблокировать");

            verify(blackListRepository, never()).save(any());
        }

        @Test
        @DisplayName("срок блокировки считается от текущего момента")
        void computesExpiryFromNow() {
            AppUser target = user();
            AppUser curator = AppUser.builder().id(1L).nickname("curator").build();
            when(festivalClient.validateCuratorAccessToUser(BEARER, 1L, 42L)).thenReturn(true);
            when(userRepository.findById(42L)).thenReturn(Optional.of(target));
            when(userRepository.findById(1L)).thenReturn(Optional.of(curator));

            BlackListAddRequest request = new BlackListAddRequest();
            request.setReason("нарушение");
            request.setDays(7);

            service.addUserToBlackList(1L, 42L, request);

            ArgumentCaptor<BlackList> saved = ArgumentCaptor.forClass(BlackList.class);
            verify(blackListRepository).save(saved.capture());
            assertThat(saved.getValue().getReason()).isEqualTo("нарушение");
            assertThat(saved.getValue().getExpiryDate())
                    .isAfter(LocalDateTime.now().plusDays(6))
                    .isBefore(LocalDateTime.now().plusDays(8));
            assertThat(saved.getValue().getBlackListId().getUser()).isSameAs(target);
            assertThat(saved.getValue().getBlackListId().getCurator()).isSameAs(curator);
        }

        @Test
        @DisplayName("без активных записей статус — не заблокирован")
        void reportsNotBlacklistedWhenNoEntries() {
            when(blackListRepository.findActiveEntries(eq(42L), any(LocalDateTime.class)))
                    .thenReturn(List.of());

            BlacklistStatusResponse status = service.getMyBlacklistStatus(42L);

            assertThat(status.isInBlacklist()).isFalse();
            assertThat(status.getReason()).isNull();
            assertThat(status.getUntil()).isNull();
        }

        @Test
        @DisplayName("активная запись отдаётся с причиной и сроком")
        void reportsActiveEntry() {
            LocalDateTime until = LocalDateTime.now().plusDays(3);
            BlackList entry = new BlackList();
            entry.setReason("нарушение правил");
            entry.setExpiryDate(until);
            when(blackListRepository.findActiveEntries(eq(42L), any(LocalDateTime.class)))
                    .thenReturn(List.of(entry));

            BlacklistStatusResponse status = service.getMyBlacklistStatus(42L);

            assertThat(status.isInBlacklist()).isTrue();
            assertThat(status.getReason()).isEqualTo("нарушение правил");
            assertThat(status.getUntil()).isEqualTo(until);
        }
    }
}
