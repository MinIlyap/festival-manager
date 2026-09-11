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
import ru.rsreu.festivalmanager.user.mapper.UserMapper;
import ru.rsreu.festivalmanager.user.repository.AppUserRepository;
import ru.rsreu.festivalmanager.user.repository.BlackListRepository;
import ru.rsreu.festivalmanager.common.util.CryptoUtil;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AppUserRepository userRepository;
    private final UserMapper userMapper;
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

    @Transactional(readOnly = true)
    public UserResponse getMe(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        String cityName = null;
        if (user.getCity_id() != null) {
            try {
                cityName = locationClient.getCityById(getBearerSystemToken(), user.getCity_id()).getName();
            } catch (Exception ignored) {}
        }

        return userMapper.toUserResponse(
                user,
                cityName,
                cryptoUtil.decrypt(user.getPassportSeriaHash()),
                cryptoUtil.decrypt(user.getPassportNumberHash()));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return userMapper.toUserProfileDto(user);
    }

    @Transactional
    public void updateUser(Long id, UserInfoUpdateRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        validateSensitiveDataAgainstBlacklist(request.getPhoneNumber(), request.getPassportSeries(), request.getPassportNumber());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getCityId() != null) {
            if (!locationClient.existsCityById(getBearerSystemToken(), request.getCityId())) {
                throw new IllegalArgumentException("Указанный город не существует");
            }
            user.setCity_id(request.getCityId());
        }

        if (request.getPassportSeries() != null && request.getPassportNumber() != null) {
            user.setPassportSeriaHash(cryptoUtil.encrypt(request.getPassportSeries()));
            user.setPassportNumberHash(cryptoUtil.encrypt(request.getPassportNumber()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void patchUser(Long id, UserInfoPatchRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        validateSensitiveDataAgainstBlacklist(request.getPhoneNumber(), request.getPassportSeries(), request.getPassportNumber());

        if (request.getCityId() != null) {
            if (!locationClient.existsCityById(getBearerSystemToken(), request.getCityId())) {
                throw new IllegalArgumentException("Указанный город не существует");
            }
            user.setCity_id(request.getCityId());
        }
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getMiddleName() != null) user.setMiddleName(request.getMiddleName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassportSeries() != null) user.setPassportSeriaHash(cryptoUtil.encrypt(request.getPassportSeries()));
        if (request.getPassportNumber() != null) user.setPassportNumberHash(cryptoUtil.encrypt(request.getPassportNumber()));

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserPublicInfo(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return Map.of("id", user.getId(), "nickname", user.getNickname());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchUsersByNickname(String nickname) {
        return userRepository.findByNicknameContainingIgnoreCase(nickname)
                .stream()
                .limit(10)
                .map(u -> Map.<String, Object>of("id", u.getId(), "nickname", u.getNickname()))
                .toList();
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

    @Transactional(readOnly = true)
    public boolean checkUserBlackListStatus(Long userId) {
        return blackListRepository.isUserBlacklistedNow(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public BlacklistStatusResponse getMyBlacklistStatus(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<BlackList> entries = blackListRepository.findActiveEntries(userId, now);
        if (entries.isEmpty()) {
            return BlacklistStatusResponse.builder().inBlacklist(false).build();
        }
        BlackList entry = entries.getFirst();
        return userMapper.toBlacklistStatusResponse(entry);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameTaken(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
