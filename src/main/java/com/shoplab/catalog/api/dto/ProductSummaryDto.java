package com.shoplab.catalog.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductSummaryDto(
        String productId,
        String categoryName,
        String categoryNameEn,
        BigDecimal referencePrice,
        int stock,
        Integer weightG,
        ProductDimensionsDto dimensions
) {}
