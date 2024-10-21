package com.example.snapheal.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class RegisterUserDto {
    private String email;
    private String username;
    private String password;
    private String fullName;
}
