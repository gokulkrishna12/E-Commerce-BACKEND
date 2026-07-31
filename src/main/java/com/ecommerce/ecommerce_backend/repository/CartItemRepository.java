package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Used when customer removes ONE item from their cart
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.id = :id")
    void deleteCartItemById(Long id);

    // Used when admin deletes a PRODUCT — clears it from everyone's cart first
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.product.id = :productId")
    void deleteByProductId(Long productId);
}