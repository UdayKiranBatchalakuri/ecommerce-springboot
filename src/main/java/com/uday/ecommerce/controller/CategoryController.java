package com.uday.ecommerce.controller;

import com.uday.ecommerce.entity.Category;
import com.uday.ecommerce.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryRepository repo;

    public CategoryController(CategoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Category> all() {
        return repo.findAll();
    }

    @PostMapping
    public Category create(@Valid @RequestBody Category c) {
        c.setId(null);
        return repo.save(c);
    }
}
