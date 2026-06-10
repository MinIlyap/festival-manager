package ru.rsreu.festivalmanager.event.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrRegistrationResponse {
    @JsonProperty("token")
    private String token;
}
