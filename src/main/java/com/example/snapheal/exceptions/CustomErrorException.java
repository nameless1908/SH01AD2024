package com.example.snapheal.exceptions;

public class CustomErrorException extends RuntimeException {
    public CustomErrorException(String message) {
        super(message);
    }

    public CustomErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}