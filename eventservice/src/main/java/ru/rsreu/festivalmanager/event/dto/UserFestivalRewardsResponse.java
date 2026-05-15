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
public class UserFestivalRewardsResponse {
    private Long festivalLocationId;
    private List<UserRewardResponse> rewards;
}
