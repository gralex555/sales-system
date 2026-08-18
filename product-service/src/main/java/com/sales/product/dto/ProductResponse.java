package com.sales.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer quantityAvailable;

    private Integer quantityReserved;

}
