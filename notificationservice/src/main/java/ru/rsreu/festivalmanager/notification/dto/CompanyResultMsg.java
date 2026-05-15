package ru.rsreu.festivalmanager.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.Data;
import ru.rsreu.festivalmanager.common.config.CustomOffsetDateTimeDeserializer;

import java.time.OffsetDateTime;

@Data
public class CompanyResultMsg {
    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("login")
    private String login;

    @JsonProperty("password_hash")
    private String passwordHash;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("created_at")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = CustomOffsetDateTimeDeserializer.class)
    private OffsetDateTime createdAt;
}