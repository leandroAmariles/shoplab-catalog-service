package com.shoplab.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<ProductCategory> findAll();
    Optional<ProductCategory> findByName(String name);
}
