package com.example.snapheal.responses;

import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ResponseObject {
    private String message;
    private HttpStatus status;
    private int code;
    private Object data;
}
