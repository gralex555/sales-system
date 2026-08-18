package com.sales.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "price is required")
    @Positive(message = "price should be more than 0")
    private BigDecimal price;

    @NotNull(message = "quantityAvailable is required")
    @PositiveOrZero(message = "quantity should be 0 or more")
    private Integer quantityAvailable;

}
