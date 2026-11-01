package com.uday.ecommerce.controller;

import com.uday.ecommerce.entity.CartItem;
import com.uday.ecommerce.entity.Product;
import com.uday.ecommerce.repository.CartItemRepository;
import com.uday.ecommerce.repository.ProductRepository;
import com.uday.ecommerce.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {

    private final CartItemRepository cartRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    public CartController(CartItemRepository cartRepo, UserRepository userRepo,
                          ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @GetMapping("/{userId}")
    public List<CartItem> view(@PathVariable Long userId) {
        return cartRepo.findByUserId(userId);
    }

    @PostMapping("/{userId}/add")
    public CartItem add(@PathVariable Long userId,
                        @RequestParam Long productId,
                        @RequestParam(defaultValue = "1") int qty) {
        if (qty < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = cartRepo.findByUserIdAndProductId(userId, productId).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUser(userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found")));
            c.setProduct(product);
            c.setQuantity(0);
            return c;
        });

        int newQty = item.getQuantity() + qty;
        if (newQty > product.getStock()) {
            throw new RuntimeException("Only " + product.getStock() + " in stock for " + product.getName());
        }
        item.setQuantity(newQty);
        return cartRepo.save(item);
    }

    @DeleteMapping("/item/{itemId}")
    public void remove(@PathVariable Long itemId) {
        cartRepo.deleteById(itemId);
    }
}
