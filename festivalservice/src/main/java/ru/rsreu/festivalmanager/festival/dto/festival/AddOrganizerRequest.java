package ru.rsreu.festivalmanager.festival.dto.festival;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddOrganizerRequest {
    @NotBlank
    @JsonProperty("tin")
    private String tin;
}
