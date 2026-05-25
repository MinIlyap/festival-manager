package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FestivalCuratorDto {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("email")
    private String email;
}
