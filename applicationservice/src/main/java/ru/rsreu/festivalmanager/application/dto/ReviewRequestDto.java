package ru.rsreu.festivalmanager.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.rsreu.festivalmanager.application.entity.ApplicationStatus;

@Data
public class ReviewRequestDto {
    @NotNull
    @JsonProperty("status")
    private ApplicationStatus status;

    @JsonProperty("comment")
    private String comment;
}
