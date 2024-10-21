package com.example.snapheal.dtos;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class LoginUserDto {
    private String email;
//    private String username;
    private String password;
}
