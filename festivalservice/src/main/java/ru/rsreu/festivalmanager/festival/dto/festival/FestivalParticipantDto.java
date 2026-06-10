package ru.rsreu.festivalmanager.festival.dto.festival;

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
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("in_blacklist")
    private boolean inBlacklist;

    @JsonProperty("blacklist_reason")
    private String blacklistReason;

    @JsonProperty("blacklist_until")
    private String blacklistUntil;
}
