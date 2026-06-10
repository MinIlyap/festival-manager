package ru.rsreu.festivalmanager.festival.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    @JsonProperty("ticket_id")
    private Long ticketId;

    @JsonProperty("festival_location_id")
    private Long festivalLocationId;

    @JsonProperty("festival_name")
    private String festivalName;

    @JsonProperty("ticket_type")
    private String ticketType;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("final_price")
    private BigDecimal finalPrice;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate;
}
