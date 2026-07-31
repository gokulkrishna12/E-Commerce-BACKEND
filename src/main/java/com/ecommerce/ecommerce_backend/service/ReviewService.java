package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ReviewDto;
import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Review> getReviewsForProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public Review addOrUpdateReview(String email, ReviewDto dto) {
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5!");
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        Review review = reviewRepository.findByProductAndUser(product, user)
                .orElse(Review.builder().product(product).user(user).build());

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setImageUrl(dto.getImageUrl());
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found!"));
        if (!review.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You can only delete your own review!");
        }
        reviewRepository.delete(review);
    }
}