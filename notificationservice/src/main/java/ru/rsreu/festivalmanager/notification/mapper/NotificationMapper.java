package ru.rsreu.festivalmanager.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rsreu.festivalmanager.notification.dto.NotificationDataDto;
import ru.rsreu.festivalmanager.notification.dto.NotificationResponse;
import ru.rsreu.festivalmanager.notification.entity.Notification;
import ru.rsreu.festivalmanager.notification.entity.NotificationData;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    // Lombok renders the isRead field as isRead(), i.e. bean property "read".
    @Mapping(target = "notificationId", source = "id")
    @Mapping(target = "isRead", source = "read")
    NotificationResponse toNotificationResponse(Notification notification);

    // companyName and festivalName have no counterpart on the entity: they stay
    // null and are hidden from the payload by @JsonInclude(NON_NULL).
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "festivalName", ignore = true)
    NotificationDataDto toNotificationDataDto(NotificationData data);
}
