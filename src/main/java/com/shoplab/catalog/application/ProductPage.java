package com.shoplab.catalog.application;

import com.shoplab.catalog.domain.Product;

import java.util.List;

public record ProductPage(
        List<Product> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static ProductPage of(List<Product> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean last = (page + 1) >= totalPages;
        return new ProductPage(content, page, size, totalElements, totalPages, last);
    }
}
