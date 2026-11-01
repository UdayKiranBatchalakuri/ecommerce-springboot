package com.uday.ecommerce.service;

import com.uday.ecommerce.entity.Category;
import com.uday.ecommerce.entity.Product;
import com.uday.ecommerce.repository.CategoryRepository;
import com.uday.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final CategoryRepository categoryRepo;

    public ProductService(ProductRepository repo, CategoryRepository categoryRepo) {
        this.repo = repo;
        this.categoryRepo = categoryRepo;
    }

    public Page<Product> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return (keyword == null || keyword.isBlank())
                ? repo.findAll(pageable)
                : repo.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public Product create(Product p) {
        p.setId(null);
        p.setCategory(resolveCategory(p.getCategory()));
        return repo.save(p);
    }

    public Product update(Long id, Product updated) {
        Product existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setImageUrl(updated.getImageUrl());
        existing.setCategory(resolveCategory(updated.getCategory()));
        return repo.save(existing);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        repo.deleteById(id);
    }

    // Replace a category that only has an id with the real one from the database.
    private Category resolveCategory(Category c) {
        if (c == null || c.getId() == null) return null;
        return categoryRepo.findById(c.getId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + c.getId()));
    }
}
