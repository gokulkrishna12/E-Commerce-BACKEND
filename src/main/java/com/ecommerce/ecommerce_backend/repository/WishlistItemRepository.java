package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUser(User user);

    boolean existsByUserAndProduct(User user, Product product);

    // ✅ Deletes by productId directly — doesn't need the Product entity
    // to still exist, so it can't fail with "Product not found"
    @Modifying
    @Transactional
    @Query("DELETE FROM WishlistItem w WHERE w.user = :user AND w.product.id = :productId")
    void deleteByUserAndProductId(User user, Long productId);

    // ✅ Used when admin deletes a PRODUCT — clears it from everyone's wishlist first
    @Modifying
    @Transactional
    @Query("DELETE FROM WishlistItem w WHERE w.product.id = :productId")
    void deleteByProductId(Long productId);
}