package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.CartDto;
import com.ecommerce.ecommerce_backend.entity.Cart;
import com.ecommerce.ecommerce_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody CartDto dto) {
        return ResponseEntity.ok(cartService.addToCart(userDetails.getUsername(), dto));
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Cart> removeFromCart(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Cart updatedCart = cartService.removeFromCart(
                cartItemId,
                userDetails.getUsername()
        );
        return ResponseEntity.ok(updatedCart);
    }
}