package ru.rsreu.festivalmanager.festival.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.festival.dto.festival.CompanyDto;
import ru.rsreu.festivalmanager.festival.dto.festival.FestivalOrganizerDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FestivalMapper {

    /**
     * The company id is taken from the local festival-company link rather than
     * from the companyservice payload, which is what the hand-written mapping
     * did; the two are expected to match but the link is authoritative here.
     */
    @Mapping(target = "companyId", source = "companyId")
    FestivalOrganizerDto toFestivalOrganizerDto(Long companyId, CompanyDto company);
}
