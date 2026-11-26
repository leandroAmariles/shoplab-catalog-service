package com.shoplab.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findProducts(long offset, int limit, String category, String query, Boolean inStock);
    long countProducts(String category, String query, Boolean inStock);
    Optional<Product> findById(String productId);
}
