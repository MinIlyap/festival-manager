package ru.rsreu.festivalmanager.festival.dto;

import lombok.Data;

@Data
public class AuthCredentialDto {
    private Long userId;
    private Long companyId;
    private String email;
    private String accessType;
}
