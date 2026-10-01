package ru.rsreu.festivalmanager.festival.dto.festival;

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

    @JsonProperty("access_type")
    private String accessType;
}
