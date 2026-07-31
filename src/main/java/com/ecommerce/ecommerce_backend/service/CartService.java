package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.CartDto;
import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Cart getCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).cartItems(new ArrayList<>()).build();
            return cartRepository.save(newCart);
        });
    }

    public Cart addToCart(String email, CartDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).cartItems(new ArrayList<>()).build();
            return cartRepository.save(newCart);
        });

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        CartItem item = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(dto.getQuantity())
                .build();

        cartItemRepository.save(item);
        return cartRepository.findById(cart.getId()).orElseThrow();
    }
    @Transactional
    public Cart removeFromCart(Long cartItemId, String email) {
        cartItemRepository.deleteCartItemById(cartItemId);
        User user = userRepository.findByEmail(email).orElseThrow();
        return cartRepository.findByUser(user).orElseThrow();
    }
}