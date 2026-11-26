package com.shoplab.catalog.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryDto(String name, String nameEn) {
}
