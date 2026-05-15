package ru.rsreu.festivalmanager.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.user.client.FestivalServiceClient;
import ru.rsreu.festivalmanager.user.client.LocationServiceClient;
import ru.rsreu.festivalmanager.user.dto.*;
import ru.rsreu.festivalmanager.user.entity.AppUser;
import ru.rsreu.festivalmanager.user.entity.BlackList;
import ru.rsreu.festivalmanager.user.entity.BlackListId;
import ru.rsreu.festivalmanager.user.repository.AppUserRepository;
import ru.rsreu.festivalmanager.user.repository.BlackListRepository;
import ru.rsreu.festivalmanager.common.util.CryptoUtil;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final BlackListRepository blackListRepository;
    private final LocationServiceClient locationClient;
    private final FestivalServiceClient festivalClient;
    private final CryptoUtil cryptoUtil;

    @Value("${internal.system-token}")
    private String systemToken;

    private String getBearerSystemToken() {
        return "Bearer " + systemToken;
    }

    @Transactional
    public Long createUser(UserCreationDto dto) {
        if (!locationClient.existsCityById(getBearerSystemToken(), dto.getCityId())) {
            throw new IllegalArgumentException("Указанный город не существует");
        }

        AppUser user = new AppUser();
        user.setNickname(dto.getNickname());
        user.setBirthDate(dto.getBirthDate());
        user.setCity_id(dto.getCityId());

        return userRepository.save(user).getId();
    }

    public UserResponse getMe(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return UserResponse.builder()
                .id(user.getId())
                .birthDate(user.getBirthDate())
                .nickname(user.getNickname())
                .cityId(user.getCity_id())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .passportSeria(cryptoUtil.decrypt(user.getPassportSeriaHash()))
                .passportNumber(cryptoUtil.decrypt(user.getPassportNumberHash()))
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return UserProfileDto.builder()
                .id(user.getId())
                .phone(user.getPhoneNumber())
                .passportData(user.getPassportNumberHash())
                .dateOfBirth(user.getBirthDate())
                .build();
    }

    @Transactional
    public void updateUser(Long id, UserInfoUpdateRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        validateSensitiveDataAgainstBlacklist(request.getPhoneNumber(), request.getPassportSeria(), request.getPassportNumber());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassportSeria() != null && request.getPassportNumber() != null) {
            user.setPassportSeriaHash(cryptoUtil.encrypt(request.getPassportSeria()));
            user.setPassportNumberHash(cryptoUtil.encrypt(request.getPassportNumber()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void patchUser(Long id, UserInfoPatchRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        validateSensitiveDataAgainstBlacklist(request.getPhoneNumber(), request.getPassportSeria(), request.getPassportNumber());

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getMiddleName() != null) user.setMiddleName(request.getMiddleName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassportSeria() != null) user.setPassportSeriaHash(cryptoUtil.encrypt(request.getPassportSeria()));
        if (request.getPassportNumber() != null) user.setPassportNumberHash(cryptoUtil.encrypt(request.getPassportNumber()));

        userRepository.save(user);
    }

    private void validateSensitiveDataAgainstBlacklist(String phone, String seria, String number) {
        LocalDateTime now = LocalDateTime.now();
        if (phone != null && blackListRepository.isPhoneInBlackList(phone, now)) {
            throw new IllegalArgumentException("Данный номер телефона привязан к заблокированному аккаунту");
        }
        if (seria != null && number != null) {
            String seriaHash = cryptoUtil.encrypt(seria);
            String numberHash = cryptoUtil.encrypt(number);
            if (blackListRepository.isPassportInBlackList(seriaHash, numberHash, now)) {
                throw new IllegalArgumentException("Данные паспортные данные привязаны к заблокированному аккаунту");
            }
        }
    }

    @Transactional
    public void addUserToBlackList(Long curatorId, Long userId, BlackListAddRequest request) {
        boolean hasAccess = festivalClient.validateCuratorAccessToUser(getBearerSystemToken(), curatorId, userId);
        if (!hasAccess) {
            throw new IllegalArgumentException("Вы не можете заблокировать этого пользователя. Он не покупал билет на ваш фестиваль.");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        AppUser curator = userRepository.findById(curatorId)
                .orElseThrow(() -> new IllegalArgumentException("Куратор не найден"));

        BlackListId id = new BlackListId();
        id.setUser(user);
        id.setCurator(curator);
        id.setCreatedAt(OffsetDateTime.now());

        BlackList blackList = new BlackList();
        blackList.setBlackListId(id);
        blackList.setReason(request.getReason());
        blackList.setExpiryDate(LocalDateTime.now().plusDays(request.getDays()));

        blackListRepository.save(blackList);
    }

    public boolean checkUserBlackListStatus(Long userId) {
        return blackListRepository.isUserBlacklistedNow(userId, LocalDateTime.now());
    }

    public boolean isNicknameTaken(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
