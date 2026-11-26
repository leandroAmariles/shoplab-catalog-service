package com.shoplab.catalog.application;

import com.shoplab.catalog.domain.CategoryRepository;
import com.shoplab.catalog.domain.ProductCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<ProductCategory> execute() {
        return categoryRepository.findAll();
    }
}
