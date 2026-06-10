package ru.rsreu.festivalmanager.event.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyQrRegistrationResponse {
    public enum VerificationStatus {
        SUCCESS, EXPIRED, FORGED, INVALID, LOCATION_MISMATCH
    }

    @JsonProperty("status")
    private VerificationStatus status;

    @JsonProperty("target_id")
    private Long targetId;

    @JsonProperty("location_id")
    private Long locationId;
}
