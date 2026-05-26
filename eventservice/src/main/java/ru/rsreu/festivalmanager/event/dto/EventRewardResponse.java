package ru.rsreu.festivalmanager.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRewardResponse {
    private Long eventRewardId;
    private Long rewardId;
    private String rewardName;
    private String description;
    private Integer place;
    private Integer pointNumber;
}
