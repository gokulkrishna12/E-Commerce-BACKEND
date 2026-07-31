package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.BuyNowDto;
import com.ecommerce.ecommerce_backend.dto.OrderDto;
import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private static final List<String> VALID_MANUAL_STATUSES = List.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED");

    @Transactional
    public Order placeOrder(String email, OrderDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart is empty!"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty!");
        }

        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getProduct().getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        "Not enough stock for \"" + cartItem.getProduct().getName() + "\"! Only "
                                + cartItem.getProduct().getStock() + " left."
                );
            }
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(dto.getShippingAddress())
                .orderItems(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItemRepository.save(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        savedOrder.setTotalAmount(total);
        savedOrder.setStatus("CONFIRMED");
        orderRepository.save(savedOrder);

        cart.getCartItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Transactional
    public Order placeDirectOrder(String email, BuyNowDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        int quantity = (dto.getQuantity() != null && dto.getQuantity() > 0) ? dto.getQuantity() : 1;

        if (product.getStock() < quantity) {
            throw new RuntimeException("Not enough stock available!");
        }
        if (dto.getShippingAddress() == null || dto.getShippingAddress().trim().isEmpty()) {
            throw new RuntimeException("Shipping address is required!");
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(dto.getShippingAddress())
                .orderItems(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build();
        orderItemRepository.save(orderItem);

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        savedOrder.setTotalAmount(total);
        savedOrder.setStatus("CONFIRMED");

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        return orderRepository.save(savedOrder);
    }

    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return orderRepository.findByUser(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order cancelOrder(Long orderId, String email, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        User user = userRepository.findByEmail(email).orElseThrow();

        boolean isCancellableByCustomer =
                order.getStatus().equals("PENDING") || order.getStatus().equals("CONFIRMED");

        if (user.getRole().equals("ROLE_USER") && !isCancellableByCustomer) {
            throw new RuntimeException("This order can no longer be cancelled — it's already on its way.");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus("CANCELLED");
        order.setShippingAddress(order.getShippingAddress() + " | Cancel reason: " + reason);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        if (!order.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Only CANCELLED orders can be deleted!");
        }

        orderRepository.delete(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        if (newStatus == null || !VALID_MANUAL_STATUSES.contains(newStatus)) {
            throw new RuntimeException("Invalid status! Must be one of: " + VALID_MANUAL_STATUSES);
        }
        if (order.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Cannot change the status of a cancelled order!");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}