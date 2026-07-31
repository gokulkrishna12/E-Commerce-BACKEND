package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.BuyNowDto;
import com.ecommerce.ecommerce_backend.dto.OrderDto;
import com.ecommerce.ecommerce_backend.dto.UpdateOrderStatusDto;
import com.ecommerce.ecommerce_backend.entity.Order;
import com.ecommerce.ecommerce_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody OrderDto dto) {
        return ResponseEntity.ok(orderService.placeOrder(userDetails.getUsername(), dto));
    }

    @PostMapping("/buy-now")
    public ResponseEntity<Order> buyNow(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody BuyNowDto dto) {
        return ResponseEntity.ok(orderService.placeDirectOrder(userDetails.getUsername(), dto));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.cancelOrder(orderId, userDetails.getUsername(), reason);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<String> deleteOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        orderService.deleteOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok("Order deleted!");
    }

    // ✅ NEW
    @PutMapping("/status/{orderId}")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusDto dto) {
        Order order = orderService.updateOrderStatus(orderId, dto.getStatus());
        return ResponseEntity.ok(order);
    }
}