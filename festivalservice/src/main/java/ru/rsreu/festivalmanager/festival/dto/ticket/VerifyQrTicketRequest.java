package ru.rsreu.festivalmanager.festival.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyQrTicketRequest {
    @JsonProperty("token")
    private String token;
}
