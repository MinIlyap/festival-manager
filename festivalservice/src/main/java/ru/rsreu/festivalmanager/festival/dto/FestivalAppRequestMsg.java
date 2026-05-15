package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.OffsetTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
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
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetTimeDeserializer.class)
    private OffsetDateTime createdAt;
}
