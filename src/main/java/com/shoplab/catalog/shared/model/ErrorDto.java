package com.shoplab.catalog.shared.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDto(String code, String description) {
}
