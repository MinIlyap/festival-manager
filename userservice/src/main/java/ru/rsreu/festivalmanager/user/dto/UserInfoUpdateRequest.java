package ru.rsreu.festivalmanager.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    private String middleName;

    @Pattern(regexp = "^\\+\\d{11,12}$", message = "Некорректный номер телефона")
    private String phoneNumber;

    @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна состоять из 4 цифр")
    private String passportSeria;

    @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должен состоять из 6 цифр")
    private String passportNumber;
}
