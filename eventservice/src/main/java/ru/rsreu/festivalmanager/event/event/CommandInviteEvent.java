package ru.rsreu.festivalmanager.event.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommandInviteEvent {
    private Long commandId;
    private String commandName;
    private Long festivalEventId;
    private Long invitedUserId;
    private Long inviterId;
}