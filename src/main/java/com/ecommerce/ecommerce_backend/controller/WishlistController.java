package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.WishlistDto;
import com.ecommerce.ecommerce_backend.entity.WishlistItem;
import com.ecommerce.ecommerce_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItem>> getWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getWishlist(userDetails.getUsername()));
    }

    @PostMapping("/add")
    public ResponseEntity<WishlistItem> addToWishlist(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody WishlistDto dto) {
        return ResponseEntity.ok(wishlistService.addToWishlist(userDetails.getUsername(), dto.getProductId()));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromWishlist(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable Long productId) {
        wishlistService.removeFromWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.ok("Removed from wishlist!");
    }
}