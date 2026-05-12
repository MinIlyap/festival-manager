package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class FestivalAppRequestMsg {
    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("festival_name")
    private String festivalName;

    @JsonProperty("sender_id")
    private Long senderId;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
