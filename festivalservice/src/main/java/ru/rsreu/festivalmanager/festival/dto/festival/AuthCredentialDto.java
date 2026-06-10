package ru.rsreu.festivalmanager.festival.dto.festival;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthCredentialDto {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("companyId")
    private Long companyId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("accessType")
    private String accessType;
}
