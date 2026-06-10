package ru.rsreu.festivalmanager.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    @NotBlank(message = "Имя обязательно")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("city_id")
    private Long cityId;

    @Pattern(regexp = "^\\d{11,12}$", message = "Некорректный номер телефона")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна состоять из 4 цифр")
    @JsonProperty("passport_series")
    private String passportSeries;

    @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должен состоять из 6 цифр")
    @JsonProperty("passport_number")
    private String passportNumber;
}
