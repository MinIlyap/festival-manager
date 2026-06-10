package ru.rsreu.festivalmanager.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("city_id")
    private Long cityId;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("passport_series")
    private String passportSeries;

    @JsonProperty("passport_number")
    private String passportNumber;
}
