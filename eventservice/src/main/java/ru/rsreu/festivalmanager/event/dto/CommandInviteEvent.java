package ru.rsreu.festivalmanager.event.dto;

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
    private Long festivalEventId;
    private String eventName;
    private Long commandId;
    private String commandName;
    private Long relatedUser; // Тот, кого приглашают
    private Long senderId;    // Тот, кто приглашает
    private OffsetDateTime createdAt;
}