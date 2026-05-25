package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCuratorRequest {
    @NotBlank
    @Email
    @JsonProperty("email")
    private String email;
}
