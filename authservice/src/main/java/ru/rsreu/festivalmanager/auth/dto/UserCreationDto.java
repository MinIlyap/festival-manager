package ru.rsreu.festivalmanager.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationDto {
    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("city_id")
    private Long cityId;
}