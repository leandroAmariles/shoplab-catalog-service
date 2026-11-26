package com.shoplab.catalog.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductStockDto(
        String productId,
        BigDecimal referencePrice,
        int stock,
        boolean available,
        Instant updatedAt
) {}
