package com.example.common.exception;

public class UpdateItemNumException extends CommonException{

    public UpdateItemNumException(String message) {
        super(message, 400);
    }

    public UpdateItemNumException(String message, Throwable cause) {
        super(message, cause, 400);
    }

    public UpdateItemNumException(Throwable cause) {
        super(cause, 400);
    }
}
