package com.example.snapheal.responses;

import com.example.snapheal.entities.User;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Builder

public class LoginResponse {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String avatar;
    private String token;
    private String refreshToken;
    private String tokenType;
}
