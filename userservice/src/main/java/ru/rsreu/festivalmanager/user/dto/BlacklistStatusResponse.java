package ru.rsreu.festivalmanager.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlacklistStatusResponse {
    private boolean inBlacklist;
    private String reason;
    private LocalDateTime until;
}
