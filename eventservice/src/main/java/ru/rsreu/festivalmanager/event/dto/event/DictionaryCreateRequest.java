package ru.rsreu.festivalmanager.event.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryCreateRequest {
    @NotBlank(message = "Название не может быть пустым")
    @JsonProperty("name")
    private String name;
}
