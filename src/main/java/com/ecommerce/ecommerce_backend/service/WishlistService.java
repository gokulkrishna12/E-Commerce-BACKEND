package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<WishlistItem> getWishlist(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return wishlistItemRepository.findByUser(user);
    }

    public WishlistItem addToWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        if (wishlistItemRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Already in your wishlist!");
        }

        WishlistItem item = WishlistItem.builder().user(user).product(product).build();
        return wishlistItemRepository.save(item);
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        // ✅ No longer looks up the Product — removal now succeeds
        // even if the product was deleted or never existed
        wishlistItemRepository.deleteByUserAndProductId(user, productId);
    }
}