package ru.rsreu.festivalmanager.event.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("command_id")
    private Long commandId;

    @JsonProperty("command_name")
    private String commandName;

    @JsonProperty("creator_id")
    private Long creatorId;

    @JsonProperty("member_user_ids")
    private List<Long> memberUserIds;
}
