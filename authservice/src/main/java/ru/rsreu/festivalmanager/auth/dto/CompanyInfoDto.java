package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompanyInfoDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("email")
    private String email;
}