package ru.rsreu.festivalmanager.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyQrRegistrationRequest {
    private String token;
}
