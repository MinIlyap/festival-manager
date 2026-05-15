package ru.rsreu.festivalmanager.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class NotificationResponse {
    @JsonProperty("notification_id")
    private Long notificationId;

    @JsonProperty("notification_type")
    private String notificationType;

    @JsonProperty("target_user_id")
    private Long targetUserId;

    @JsonProperty("is_read")
    private Boolean isRead;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private NotificationDataDto data;
}
