package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

@Data
public class BuyNowDto {
    private Long productId;
    private Integer quantity;
    private String shippingAddress;
}