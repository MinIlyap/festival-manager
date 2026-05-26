package ru.rsreu.festivalmanager.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandProductResponse {
    private Long productInstanceId;
    private Long productId;
    private String productName;
    private String description;
    private Integer quantity;
    private BigDecimal price;
}
