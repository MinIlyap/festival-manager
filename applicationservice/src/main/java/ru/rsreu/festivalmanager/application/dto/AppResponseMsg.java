package ru.rsreu.festivalmanager.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class AppResponseMsg {
    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}
