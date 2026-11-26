package com.shoplab.catalog.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDetailDto(
        String productId,
        String categoryName,
        String categoryNameEn,
        Integer nameLength,
        Integer descriptionLength,
        Integer photosCount,
        Integer weightG,
        ProductDimensionsDto dimensions,
        BigDecimal referencePrice,
        int stock,
        boolean available
) {}
