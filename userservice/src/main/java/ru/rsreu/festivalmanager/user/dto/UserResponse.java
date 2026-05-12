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

    @JsonProperty("access_type")
    private String accessType;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("city_id")
    private Long cityId;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("passport_seria")
    private String passportSeria;

    @JsonProperty("passport_number")
    private String passportNumber;
}
