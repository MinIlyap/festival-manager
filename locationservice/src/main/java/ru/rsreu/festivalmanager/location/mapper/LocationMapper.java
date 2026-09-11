package ru.rsreu.festivalmanager.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.location.dto.CityResponse;
import ru.rsreu.festivalmanager.location.dto.PlatformInstanceResponse;
import ru.rsreu.festivalmanager.location.entity.City;
import ru.rsreu.festivalmanager.location.entity.PlatformInstance;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {

    CityResponse toCityResponse(City city);

    @Mapping(target = "platformName", source = "platform.name")
    PlatformInstanceResponse toPlatformInstanceResponse(PlatformInstance instance);
}
