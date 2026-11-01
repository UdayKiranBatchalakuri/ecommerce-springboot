package com.uday.ecommerce.service;

import com.uday.ecommerce.entity.CartItem;
import com.uday.ecommerce.entity.Order;
import com.uday.ecommerce.entity.OrderItem;
import com.uday.ecommerce.entity.Product;
import com.uday.ecommerce.repository.CartItemRepository;
import com.uday.ecommerce.repository.OrderRepository;
import com.uday.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private static final Set<String> STATUSES =
            Set.of("PLACED", "PAID", "SHIPPED", "DELIVERED", "CANCELLED");

    private final CartItemRepository cartRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderService(CartItemRepository cartRepo, OrderRepository orderRepo,
                        ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    /**
     * Converts the user's cart into an order.
     * Runs in ONE transaction: if anything fails (e.g. not enough stock),
     * everything is rolled back and no stock is reduced.
     */
    @Transactional
    public Order checkout(Long userId) {
        List<CartItem> cart = cartRepo.findByUserId(userId);
        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(cart.get(0).getUser());
        order.setStatus("PLACED");
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cart) {
            Product p = ci.getProduct();
            if (p.getStock() < ci.getQuantity()) {
                throw new RuntimeException("Not enough stock for " + p.getName()
                        + " (available: " + p.getStock() + ")");
            }
            p.setStock(p.getStock() - ci.getQuantity());
            productRepo.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(p.getPrice());
            order.getItems().add(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setTotal(total);
        Order saved = orderRepo.save(order);
        cartRepo.deleteAll(cart);
        return saved;
    }

    public List<Order> history(Long userId) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> all() {
        return orderRepo.findAll();
    }

    @Transactional
    public Order updateStatus(Long orderId, String status) {
        String s = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(s)) {
            throw new RuntimeException("Invalid status. Use one of: " + STATUSES);
        }
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        o.setStatus(s);
        return orderRepo.save(o);
    }
}
