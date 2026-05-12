package ru.rsreu.festivalmanager.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("passport_data")
    private String passportData;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
}
