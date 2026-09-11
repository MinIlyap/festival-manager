package ru.rsreu.festivalmanager.company.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.company.dto.CompanyResponse;
import ru.rsreu.festivalmanager.company.entity.Company;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

    // Lombok turns the isActive field into an isActive() getter, so the bean
    // property on the entity side is "active" while the dto builder keeps isActive.
    @Mapping(target = "isActive", source = "active")
    CompanyResponse toCompanyResponse(Company company);
}
