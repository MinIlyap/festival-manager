package ru.rsreu.festivalmanager.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandInviteEvent {
    @JsonProperty("festival_event_id")
    private Long festivalEventId;
    @JsonProperty("event_name")
    private String eventName;
    @JsonProperty("command_id")
    private Long commandId;
    @JsonProperty("command_name")
    private String commandName;
    @JsonProperty("related_user")
    private Long relatedUser;
    @JsonProperty("sender_id")
    private Long senderId;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}