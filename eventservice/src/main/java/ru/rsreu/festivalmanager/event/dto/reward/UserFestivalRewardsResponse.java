package ru.rsreu.festivalmanager.event.dto.reward;

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
public class UserFestivalRewardsResponse {
    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("rewards")
    private List<UserRewardResponse> rewards;
}
