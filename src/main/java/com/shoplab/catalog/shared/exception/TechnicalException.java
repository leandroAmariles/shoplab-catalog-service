package com.shoplab.catalog.shared.exception;

public class TechnicalException extends RuntimeException {

    private final ResponseCodeEnum errorCode;

    public TechnicalException(ResponseCodeEnum errorCode, Throwable cause) {
        super(errorCode.getDescription(), cause);
        this.errorCode = errorCode;
    }

    public TechnicalException(ResponseCodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ResponseCodeEnum getErrorCode() {
        return errorCode;
    }
}
