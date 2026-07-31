package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String imageUrl;
}