package com.shoplab.catalog.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDimensionsDto(Integer lengthCm, Integer heightCm, Integer widthCm) {
}
