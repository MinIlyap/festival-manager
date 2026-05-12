package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FestivalAppResponseMsg {
    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("target_user")
    private Long targetUser;
}
