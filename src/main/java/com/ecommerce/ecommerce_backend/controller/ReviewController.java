package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ReviewDto;
import com.ecommerce.ecommerce_backend.entity.Review;
import com.ecommerce.ecommerce_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsForProduct(productId));
    }

    @PostMapping("/add")
    public ResponseEntity<Review> addReview(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody ReviewDto dto) {
        return ResponseEntity.ok(reviewService.addOrUpdateReview(userDetails.getUsername(), dto));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ResponseEntity.ok("Review deleted!");
    }
}