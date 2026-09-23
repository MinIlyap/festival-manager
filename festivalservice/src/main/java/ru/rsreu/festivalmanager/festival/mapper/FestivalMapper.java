package ru.rsreu.festivalmanager.festival.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.festival.dto.festival.CompanyDto;
import ru.rsreu.festivalmanager.festival.dto.festival.FestivalOrganizerDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FestivalMapper {

    @Mapping(target = "companyId", source = "companyId")
    FestivalOrganizerDto toFestivalOrganizerDto(Long companyId, CompanyDto company);
}
