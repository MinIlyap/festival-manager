package ru.rsreu.festivalmanager.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDataDto {
    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_email")
    private String companyEmail;

    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("festival_name")
    private String festivalName;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("event_id")
    private Long eventId;

    @JsonProperty("event_name")
    private String eventName;

    @JsonProperty("command_id")
    private Long commandId;

    @JsonProperty("command_name")
    private String commandName;

    @JsonProperty("related_user_id")
    private Long relatedUserId;
}
