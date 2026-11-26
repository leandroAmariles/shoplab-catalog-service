package com.shoplab.catalog.api.dto;

import java.util.List;

public record ProductPageDto(
        List<ProductSummaryDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
