package com.example.snapheal.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Builder
public class LoginResponse {
    private String token;
    private long expiresIn;
}
