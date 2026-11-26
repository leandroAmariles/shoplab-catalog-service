package com.shoplab.catalog.shared.exception;

import com.shoplab.catalog.shared.logging.SecureLogger;
import com.shoplab.catalog.shared.model.ErrorDto;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final SecureLogger log = SecureLogger.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDto> handleBusinessException(BusinessException ex, ServerWebExchange exchange) {
        log.warn("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
                .body(new ErrorDto(ex.getErrorCode().getCode(), ex.getMessage()));
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorDto> handleTechnicalException(TechnicalException ex, ServerWebExchange exchange) {
        log.error("Technical exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
                .body(new ErrorDto(ex.getErrorCode().getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorDto> handleValidationException(ServerWebInputException ex, ServerWebExchange exchange) {
        log.warn("Invalid input parameters: {}", ex.getReason());
        ResponseCodeEnum code = ResponseCodeEnum.INVALID_PARAMETERS;
        return ResponseEntity.status(code.getHttpStatus())
                .body(new ErrorDto(code.getCode(), ex.getReason() != null ? ex.getReason() : code.getDescription()));
    }

    /** Framework errors (unknown path, wrong method or media type) keep their status instead of becoming a 500. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDto> handleResponseStatusException(ResponseStatusException ex) {
        ResponseCodeEnum code = switch (ex.getStatusCode().value()) {
            case 404 -> ResponseCodeEnum.RESOURCE_NOT_FOUND;
            case 405 -> ResponseCodeEnum.METHOD_NOT_ALLOWED;
            default -> ResponseCodeEnum.INVALID_PARAMETERS;
        };
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorDto(code.getCode(), code.getDescription()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDto> handleDataAccessException(DataAccessException ex, ServerWebExchange exchange) {
        log.error("Database access failure", ex);
        ResponseCodeEnum code = ResponseCodeEnum.DATABASE_UNAVAILABLE;
        return ResponseEntity.status(code.getHttpStatus())
                .body(new ErrorDto(code.getCode(), code.getDescription()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error in catalog-service", ex);
        ResponseCodeEnum code = ResponseCodeEnum.UNKNOWN_ERROR;
        return ResponseEntity.status(code.getHttpStatus())
                .body(new ErrorDto(code.getCode(), code.getDescription()));
    }
}
