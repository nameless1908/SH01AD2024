package com.example.snapheal.controller;

import com.example.snapheal.dtos.LoginUserDto;
import com.example.snapheal.dtos.RefreshTokenDto;
import com.example.snapheal.dtos.RegisterUserDto;
import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.entities.User;
import com.example.snapheal.responses.LoginResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.AuthenticationService;
import com.example.snapheal.service.JwtService;
import com.example.snapheal.service.RefreshTokenService;
import com.example.snapheal.service.UserService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${api.prefix}/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, RefreshTokenService refreshTokenService, UserService userService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(
                ResponseObject.builder()
                .message("Register successfully!")
                .status(HttpStatus.OK)
                .data(registeredUser)
                .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> authenticate(@RequestBody LoginUserDto loginUserDto) {

        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);
        ;
        RefreshToken refreshToken = refreshTokenService.save(authenticatedUser, jwtToken);
        LoginResponse loginResponse = LoginResponse.builder()
                .email(authenticatedUser.getEmail())
                .username(authenticatedUser.getUsername())
                .fullName(authenticatedUser.getFullName())
                .avatar(authenticatedUser.getAvatar())
                .token(jwtToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(loginResponse)
                        .status(HttpStatus.OK)
                        .message("Successfully!")
                        .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseObject> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {
        User userDetails = userService.getUserDetailsFromRefreshToken(refreshTokenDto.getRefreshToken());
        RefreshToken token = refreshTokenService.refreshToken(refreshTokenDto.getRefreshToken(), userDetails);
        LoginResponse loginResponse = LoginResponse.builder()
                .email(userDetails.getEmail())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .avatar(userDetails.getAvatar())
                .token(token.getToken())
                .refreshToken(token.getRefreshToken())
                .build();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Successfully!")
                        .status(HttpStatus.OK)
                        .data(loginResponse)
                        .build()
        );
    }
}