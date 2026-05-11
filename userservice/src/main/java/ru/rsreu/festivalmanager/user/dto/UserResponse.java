package ru.rsreu.festivalmanager.user.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String accessType;
    private LocalDate birthDate;
    private String nickname;
    private Long cityId;
    private String phoneNumber;
    private String lastName;
    private String firstName;
    private String middleName;
    private String passportSeria;
    private String passportNumber;
}
