package com.kalasetu.web.controller;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.model.CraftCategory;
import com.kalasetu.domain.model.CraftProduct;
import com.kalasetu.domain.model.VerifiedReview;
import com.kalasetu.domain.security.InputSanitizer;
import com.kalasetu.persistence.MarketplaceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final MarketplaceRepository repository;

    public CatalogController(MarketplaceRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/categories")
    public List<CraftCategory> listCategories() {
        return repository.findAllCategories();
    }

    @GetMapping("/products")
    public List<CraftProduct> listProducts() {
        return repository.findActiveProducts();
    }

    @GetMapping("/products/search")
    public List<CraftProduct> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "false") boolean giOnly) throws InvalidInputException {
        String cleanQuery = null;
        if (q != null && !q.isBlank()) {
            cleanQuery = InputSanitizer.sanitizeText("searchQuery", q, 80);
        }
        return repository.searchProducts(cleanQuery, categoryId, giOnly);
    }

    @GetMapping("/products/{id}/reviews")
    public List<VerifiedReview> productReviews(@PathVariable UUID id) {
        return repository.findReviewsByProduct(id);
    }
}
