package ru.rsreu.festivalmanager.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.Data;
import ru.rsreu.festivalmanager.common.config.CustomOffsetDateTimeDeserializer;

import java.time.OffsetDateTime;

@Data
public class AdminNotificationMsg {
    @JsonProperty("agentId")
    private Long agentId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("entityId")
    private Long entityId;

    @JsonProperty("updated_at")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = CustomOffsetDateTimeDeserializer.class)
    private OffsetDateTime updatedAt;
}
