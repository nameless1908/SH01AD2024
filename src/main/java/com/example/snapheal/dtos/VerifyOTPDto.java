package com.example.snapheal.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VerifyOTPDto {
    private String email;
    private String otpToken;
}
