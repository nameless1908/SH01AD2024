package com.example.snapheal.controller;

import com.example.snapheal.dtos.LoginUserDto;
import com.example.snapheal.dtos.RegisterUserDto;
import com.example.snapheal.entities.User;
import com.example.snapheal.responses.LoginResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.AuthenticationService;
import com.example.snapheal.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${api.prefix}/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> authenticate(@RequestBody LoginUserDto loginUserDto) {

        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(loginResponse)
                        .status(HttpStatus.OK)
                        .message("Successfully!")
                        .build()
        );
    }
}