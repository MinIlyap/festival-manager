package ru.rsreu.festivalmanager.event.dto.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRewardResponse {
    @JsonProperty("event_reward_id")
    private Long eventRewardId;

    @JsonProperty("reward_id")
    private Long rewardId;

    @JsonProperty("reward_name")
    private String rewardName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("place")
    private Integer place;

    @JsonProperty("point_number")
    private Integer pointNumber;
}
