package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalParticipantDto {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("inBlacklist")
    private boolean inBlacklist;

    @JsonProperty("blacklistReason")
    private String blacklistReason;

    @JsonProperty("blacklistUntil")
    private String blacklistUntil;
}
