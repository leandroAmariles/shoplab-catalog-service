package com.shoplab.catalog.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Catalogo de codigos de error tipados para catalog-service (SLHD_CATALOG_*).
 */
public enum ResponseCodeEnum {

    PRODUCT_NOT_FOUND("SLHD_CATALOG_00001", "Producto no encontrado en catalogo", HttpStatus.NOT_FOUND),
    INVALID_PARAMETERS("SLHD_CATALOG_00002", "Parametros de consulta o paginacion invalidos",
            HttpStatus.UNPROCESSABLE_ENTITY),
    DATABASE_UNAVAILABLE("SLHD_CATALOG_00003", "Almacen de base de datos no disponible", HttpStatus.SERVICE_UNAVAILABLE),
    METHOD_NOT_ALLOWED("SLHD_CATALOG_00004", "Metodo HTTP no permitido para este recurso",
            HttpStatus.METHOD_NOT_ALLOWED),
    RESOURCE_NOT_FOUND("SLHD_CATALOG_00005", "Recurso no encontrado", HttpStatus.NOT_FOUND),
    UNKNOWN_ERROR("SLHD_CATALOG_99999", "Error inesperado en catalog-service", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String description;
    private final HttpStatus httpStatus;

    ResponseCodeEnum(String code, String description, HttpStatus httpStatus) {
        this.code = code;
        this.description = description;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
