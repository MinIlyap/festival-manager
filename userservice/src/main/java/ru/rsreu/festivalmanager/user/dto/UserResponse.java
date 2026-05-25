package ru.rsreu.festivalmanager.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String nickname;
    private LocalDate birthDate;
    private Long cityId;
    private String cityName;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private String passportSeries;
    private String passportNumber;
}
