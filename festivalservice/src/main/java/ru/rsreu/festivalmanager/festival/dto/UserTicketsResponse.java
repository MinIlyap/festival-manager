package ru.rsreu.festivalmanager.festival.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserTicketsResponse {
    @JsonProperty("valid_tickets")
    private List<TicketResponse> validTickets;

    @JsonProperty("expired_tickets")
    private List<TicketResponse> expiredTickets;
}
