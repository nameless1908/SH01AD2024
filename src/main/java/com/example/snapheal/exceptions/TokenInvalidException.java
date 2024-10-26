package com.example.snapheal.exceptions;

public class TokenInvalidException extends RuntimeException {
    public TokenInvalidException(String message) {
        super(message);
    }
}
