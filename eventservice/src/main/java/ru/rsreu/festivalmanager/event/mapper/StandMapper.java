package ru.rsreu.festivalmanager.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.event.dto.stand.FestivalStandResponse;
import ru.rsreu.festivalmanager.event.dto.stand.StandProductResponse;
import ru.rsreu.festivalmanager.event.entity.stand.FestivalStand;
import ru.rsreu.festivalmanager.event.entity.stand.StandProduct;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StandMapper {

    @Mapping(target = "name", source = "standInstance.stand.name")
    @Mapping(target = "type", source = "standInstance.standType")
    @Mapping(target = "description", source = "standInstance.description")
    @Mapping(target = "companyId", source = "standInstance.companyId")
    @Mapping(target = "organizerCompanyId", source = "standInstance.companyId")
    @Mapping(target = "dateStart", source = "standInstance.dateStart")
    FestivalStandResponse toFestivalStandResponse(FestivalStand festivalStand);

    @Mapping(target = "productInstanceId", source = "standProductId.productInstance.id")
    @Mapping(target = "productId", source = "standProductId.productInstance.product.id")
    @Mapping(target = "productName", source = "standProductId.productInstance.product.name")
    @Mapping(target = "description", source = "standProductId.productInstance.description")
    @Mapping(target = "quantity", source = "standProductId.productInstance.quantity")
    @Mapping(target = "price", source = "standProductId.productInstance.price")
    StandProductResponse toStandProductResponse(StandProduct standProduct);
}
