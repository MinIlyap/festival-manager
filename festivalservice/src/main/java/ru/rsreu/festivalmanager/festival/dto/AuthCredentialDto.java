package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthCredentialDto {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("email")
    private String email;
}
