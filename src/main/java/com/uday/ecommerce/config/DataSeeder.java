package com.uday.ecommerce.config;

import com.uday.ecommerce.entity.Category;
import com.uday.ecommerce.entity.Product;
import com.uday.ecommerce.entity.User;
import com.uday.ecommerce.repository.CategoryRepository;
import com.uday.ecommerce.repository.ProductRepository;
import com.uday.ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Adds demo data on first start so the store is not empty. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DataSeeder(CategoryRepository categories, ProductRepository products,
                      UserRepository users, PasswordEncoder encoder) {
        this.categories = categories;
        this.products = products;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.findByEmail("admin@store.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@store.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole("ADMIN");
            users.save(admin);
        }

        if (products.count() > 0) return;

        Category electronics = cat("Electronics");
        Category fashion = cat("Fashion");
        Category books = cat("Books");

        product("Wireless Mouse", "Bluetooth + 2.4GHz, silent clicks", "799", 50, electronics);
        product("Mechanical Keyboard", "Blue switches, RGB backlight", "2499", 25, electronics);
        product("Bluetooth Headphones", "30 hour battery, deep bass", "1999", 30, electronics);
        product("Smart Watch", "Heart-rate, sleep and step tracking", "3499", 15, electronics);
        product("Cotton T-Shirt", "100% cotton, regular fit", "499", 100, fashion);
        product("Denim Jacket", "Classic blue denim", "1799", 20, fashion);
        product("Running Shoes", "Lightweight and breathable", "2299", 40, fashion);
        product("Java: The Complete Reference", "Comprehensive Java guide", "899", 35, books);
        product("Clean Code", "A handbook of agile software craftsmanship", "749", 30, books);
        product("Data Structures Made Easy", "Interview-oriented DSA problems", "599", 45, books);
    }

    private Category cat(String name) {
        Category c = new Category();
        c.setName(name);
        return categories.save(c);
    }

    private void product(String name, String desc, String price, int stock, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setCategory(category);
        p.setImageUrl("https://placehold.co/400x300?text=" + name.replace(" ", "+"));
        products.save(p);
    }
}
