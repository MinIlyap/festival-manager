package ru.rsreu.festivalmanager.auth.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import ru.rsreu.festivalmanager.common.config.CustomOffsetDateTimeDeserializer;

import java.time.OffsetDateTime;

@Data
@Builder
public class CompanyAuthMsg {
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
