package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

@Data
public class CartDto {
    private Long productId;
    private Integer quantity;
}