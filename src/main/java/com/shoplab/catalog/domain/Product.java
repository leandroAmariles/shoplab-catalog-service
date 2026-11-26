package com.shoplab.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Product(
        String productId,
        String categoryName,
        String categoryNameEn,
        Integer nameLength,
        Integer descriptionLength,
        Integer photosCount,
        Integer weightG,
        ProductDimensions dimensions,
        BigDecimal referencePrice,
        int stock,
        Instant updatedAt
) {
    public boolean isAvailable() {
        return stock > 0;
    }
}
