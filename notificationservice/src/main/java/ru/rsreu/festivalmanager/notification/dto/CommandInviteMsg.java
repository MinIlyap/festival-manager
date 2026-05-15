package ru.rsreu.festivalmanager.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.Data;
import ru.rsreu.festivalmanager.common.config.CustomOffsetDateTimeDeserializer;

import java.time.OffsetDateTime;

@Data
public class CommandInviteMsg {
    @JsonProperty("festival_event_id")
    private Long eventId;

    @JsonProperty("event_name")
    private String eventName;

    @JsonProperty("command_id")
    private Long commandId;

    @JsonProperty("command_name")
    private String commandName;

    @JsonProperty("related_user")
    private Long relatedUserId;

    @JsonProperty("sender_id")
    private Long senderId;

    @JsonProperty("created_at")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = CustomOffsetDateTimeDeserializer.class)
    private OffsetDateTime createdAt;
}
