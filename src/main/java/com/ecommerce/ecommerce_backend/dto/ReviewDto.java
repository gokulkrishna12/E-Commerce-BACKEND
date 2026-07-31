package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long productId;
    private Integer rating;
    private String comment;
    private String imageUrl;
}