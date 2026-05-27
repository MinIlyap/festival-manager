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
public class BlacklistStatusDto {
    @JsonProperty("inBlacklist")
    private boolean inBlacklist;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("until")
    private String until;
}
