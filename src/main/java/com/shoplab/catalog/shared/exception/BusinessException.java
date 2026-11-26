package com.shoplab.catalog.shared.exception;

public class BusinessException extends RuntimeException {

    private final ResponseCodeEnum errorCode;

    public BusinessException(ResponseCodeEnum errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public BusinessException(ResponseCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResponseCodeEnum getErrorCode() {
        return errorCode;
    }
}
