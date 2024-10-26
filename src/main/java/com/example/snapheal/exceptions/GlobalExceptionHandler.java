package com.example.snapheal.exceptions;

import com.example.snapheal.responses.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseObject> handleGeneralException(Exception exception) {
        return ResponseEntity.internalServerError().body(
                ResponseObject.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(TokenInvalidException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseObject> handleRefreshTokenNotFoundException(TokenInvalidException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseObject.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(CustomErrorException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResponseObject> handleDataNotFoundException(CustomErrorException ex) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .code(2300)
                        .message(ex.getMessage())
                        .data(null) // hoặc có thể trả về dữ liệu liên quan
                        .build()
        );
    }
}
