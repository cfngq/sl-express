package com.example.common.exception;

public class NoItemException extends CommonException{

    public NoItemException(String message) {
        super(message, 400);
    }

    public NoItemException(String message, Throwable cause) {
        super(message, cause, 400);
    }

    public NoItemException(Throwable cause) {
        super(cause, 400);
    }
}
