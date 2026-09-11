package ru.rsreu.festivalmanager.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.user.dto.BlacklistStatusResponse;
import ru.rsreu.festivalmanager.user.dto.UserProfileDto;
import ru.rsreu.festivalmanager.user.dto.UserResponse;
import ru.rsreu.festivalmanager.user.entity.AppUser;
import ru.rsreu.festivalmanager.user.entity.BlackList;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * The city name is fetched from locationservice and the passport fields are
     * decrypted, both of which stay in the service; they are passed in as
     * separate arguments so this mapper remains a pure field copy.
     */
    @Mapping(target = "cityId", source = "user.city_id")
    @Mapping(target = "cityName", source = "cityName")
    @Mapping(target = "passportSeries", source = "passportSeries")
    @Mapping(target = "passportNumber", source = "passportNumber")
    UserResponse toUserResponse(AppUser user, String cityName, String passportSeries, String passportNumber);

    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "passportData", source = "passportNumberHash")
    @Mapping(target = "dateOfBirth", source = "birthDate")
    UserProfileDto toUserProfileDto(AppUser user);

    // Only called for an active entry, so the flag is always true.
    @Mapping(target = "inBlacklist", constant = "true")
    @Mapping(target = "until", source = "expiryDate")
    BlacklistStatusResponse toBlacklistStatusResponse(BlackList entry);
}
