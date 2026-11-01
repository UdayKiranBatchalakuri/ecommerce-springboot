package com.uday.ecommerce.controller;

import com.uday.ecommerce.entity.User;
import com.uday.ecommerce.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody User u) {
        if (repo.findByEmail(u.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        u.setId(null);
        u.setPassword(encoder.encode(u.getPassword()));
        u.setRole("USER"); // nobody can self-register as ADMIN
        return repo.save(u);
    }

    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            throw new RuntimeException("Email and password are required");
        }
        User u = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!encoder.matches(password, u.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return u;
    }
}
