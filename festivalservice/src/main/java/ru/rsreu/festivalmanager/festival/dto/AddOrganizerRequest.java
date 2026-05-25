package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddOrganizerRequest {
    @NotBlank
    @JsonProperty("tin")
    private String tin;
}
