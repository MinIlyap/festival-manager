package ru.rsreu.festivalmanager.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRewardResponse {
    private Long eventRewardId;
    private String eventName;
    private String rewardName;
    private String description;
    private Integer place;
    private Integer pointNumber;
}
