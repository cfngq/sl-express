package com.example.common.exception;

public class BadApplyPayOrderException extends CommonException{

    public BadApplyPayOrderException(String message) {
        super(message, 400);
    }

    public BadApplyPayOrderException(String message, Throwable cause) {
        super(message, cause, 400);
    }

    public BadApplyPayOrderException(Throwable cause) {
        super(cause, 400);
    }
}
