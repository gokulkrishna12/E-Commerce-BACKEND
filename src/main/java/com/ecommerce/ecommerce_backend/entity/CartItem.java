package com.ecommerce.ecommerce_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnoreProperties("cartItems")  // ← ADD THIS
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties("cartItems")
    private Product product;

    private Integer quantity;
}