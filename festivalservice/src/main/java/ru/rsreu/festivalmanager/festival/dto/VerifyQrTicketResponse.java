package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyQrTicketResponse {
    public enum VerificationStatus {
        SUCCESS, EXPIRED, FORGED, INVALID, LOCATION_MISMATCH
    }

    @JsonProperty("status")
    private VerificationStatus status;

    @JsonProperty("target_id")
    private Long targetId;

    @JsonProperty("location_id")
    private Long locationId;

    @JsonProperty("user_id")
    private Long userId;
}
