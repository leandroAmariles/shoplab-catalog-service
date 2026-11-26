package com.shoplab.catalog.shared.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseDto<T>(T data, ErrorDto error) {
    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(data, null);
    }

    public static <T> ResponseDto<T> error(ErrorDto error) {
        return new ResponseDto<>(null, error);
    }
}
