package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ProductDto;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.repository.CartItemRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.ReviewRepository;
import com.ecommerce.ecommerce_backend.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ReviewRepository reviewRepository;

    public Product addProduct(ProductDto dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .build();
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));
    }

    public Product updateProduct(Long id, ProductDto dto) {
        Product product = getProductById(id);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);

        cartItemRepository.deleteByProductId(id);
        wishlistItemRepository.deleteByProductId(id);
        reviewRepository.deleteByProductId(id);   // ← the missing piece

        try {
            productRepository.delete(product);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "Cannot delete '" + product.getName() + "' — it has existing order history."
            );
        }
    }

    public List<Product> getByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
}