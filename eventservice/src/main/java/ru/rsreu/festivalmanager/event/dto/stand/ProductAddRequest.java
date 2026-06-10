package ru.rsreu.festivalmanager.event.dto.stand;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAddRequest {
    @NotNull(message = "ID базового продукта (из словаря) обязателен")
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Количество товара обязательно")
    @Min(value = 1, message = "Количество не может быть меньше 1")
    @JsonProperty("quantity")
    private Integer quantity;

    @NotNull(message = "Цена товара обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    @JsonProperty("price")
    private BigDecimal price;
}
