package ru.rsreu.festivalmanager.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandWithMembersResponse {
    private Long commandId;
    private String commandName;
    private Long creatorId;
    private List<Long> memberUserIds;
}
