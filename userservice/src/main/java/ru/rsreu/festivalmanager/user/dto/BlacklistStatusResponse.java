package ru.rsreu.festivalmanager.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlacklistStatusResponse {
    @JsonProperty("in_blacklist")
    private boolean inBlacklist;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("until")
    private LocalDateTime until;
}
