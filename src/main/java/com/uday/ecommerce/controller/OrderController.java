package com.uday.ecommerce.controller;

import com.uday.ecommerce.entity.Order;
import com.uday.ecommerce.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/{userId}/checkout")
    public Order checkout(@PathVariable Long userId) {
        return service.checkout(userId);
    }

    @GetMapping("/{userId}")
    public List<Order> history(@PathVariable Long userId) {
        return service.history(userId);
    }

    // Admin-style endpoints (no JWT in this version - see README)
    @GetMapping("/all")
    public List<Order> all() {
        return service.all();
    }

    @PutMapping("/{orderId}/status")
    public Order updateStatus(@PathVariable Long orderId, @RequestParam String status) {
        return service.updateStatus(orderId, status);
    }
}
