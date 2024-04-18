package com.example.common.exception;

public class BadCancelOrderException extends CommonException{

    public BadCancelOrderException(String message) {
        super(message, 400);
    }

    public BadCancelOrderException(String message, Throwable cause) {
        super(message, cause, 400);
    }

    public BadCancelOrderException(Throwable cause) {
        super(cause, 400);
    }
}
