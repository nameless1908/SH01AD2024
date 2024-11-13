package com.example.snapheal.controller;

import com.example.snapheal.dtos.*;
import com.example.snapheal.entities.OTPToken;
import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.entities.User;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.responses.LoginResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.responses.VerifyOTPResponse;
import com.example.snapheal.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("${api.prefix}/auth")
@RestController
public class AuthenticationController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;
    @Autowired
    private OTPTokenService otpTokenService;
    @Autowired
    private EmailService emailService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, RefreshTokenService refreshTokenService, UserService userService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@RequestBody RegisterUserDto registerUserDto) throws Exception {
        User registeredUser = authenticationService.signup(registerUserDto);
        String jwtToken = jwtService.generateToken(registeredUser);
        ;
        RefreshToken refreshToken = refreshTokenService.save(registeredUser, jwtToken);
        LoginResponse loginResponse = LoginResponse.builder()
                .id(registeredUser.getId())
                .email(registeredUser.getEmail())
                .username(registeredUser.getUsername())
                .fullName(registeredUser.getFullName())
                .avatar(registeredUser.getAvatar())
                .token(jwtToken)
                .refreshToken(refreshToken.getRefreshToken())
                .tokenType("Bearer ")
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Register successfully!")
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .data(loginResponse)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> authenticate(@RequestBody LoginUserDto loginUserDto) throws Exception {

        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);
        ;
        RefreshToken refreshToken = refreshTokenService.save(authenticatedUser, jwtToken);
        LoginResponse loginResponse = LoginResponse.builder()
                .id(authenticatedUser.getId())
                .email(authenticatedUser.getEmail())
                .username(authenticatedUser.getUsername())
                .fullName(authenticatedUser.getFullName())
                .avatar(authenticatedUser.getAvatar())
                .token(jwtToken)
                .refreshToken(refreshToken.getRefreshToken())
                .tokenType("Bearer ")
                .build();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(loginResponse)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Successfully!")
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        final String jwt = authHeader.substring(7);
        RefreshToken token = refreshTokenService.findByToken(jwt);
        token.setRevoked(true);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(true)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Successfully!")
                        .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseObject> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {
        User userDetails = userService.getUserDetailsFromRefreshToken(refreshTokenDto.getRefreshToken());
        RefreshToken token = refreshTokenService.refreshToken(userDetails);
        LoginResponse loginResponse = LoginResponse.builder()
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .avatar(userDetails.getAvatar())
                .token(token.getToken())
                .refreshToken(token.getRefreshToken())
                .tokenType("Bearer ")
                .build();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Successfully!")
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .data(loginResponse)
                        .build()
        );
    }


    // Reset Password
    @GetMapping("/send-otp")
    public ResponseEntity<ResponseObject> authenticate(@RequestParam String email) throws Exception {
        OTPToken otpToken = otpTokenService.createNewOTPToken(email);
        emailService.sendResetPasswordEmail(otpToken.getUser().getEmail(), otpToken.getToken());
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(true)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Send email successfully!")
                        .build()
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseObject> verifyOtp(@RequestBody VerifyOTPDto verifyOTPDto) throws Exception {
        String uuidString = otpTokenService.validateToken(verifyOTPDto.getEmail(), verifyOTPDto.getOtpToken());
        VerifyOTPResponse response = VerifyOTPResponse.builder()
                .verifyOTPToken(uuidString)
                .build();
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(response)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("OTP Verified!")
                        .build()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseObject> verifyOtp(@RequestBody ResetPasswordDto resetPasswordDto) throws Exception {
        Boolean verifyTokenValid = otpTokenService.validateVerifyOTPToken(resetPasswordDto.getEmail(), resetPasswordDto.getVerifyOTPToken());
        if (!verifyTokenValid) {
            throw new CustomErrorException("Verify Token not match!");
        }
        User user = userService.findUserByEmail(resetPasswordDto.getEmail()).orElseThrow(
                () -> new CustomErrorException("Not found user by email!")
        );
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        String jwtToken = jwtService.generateToken(user);
        ;
        RefreshToken refreshToken = refreshTokenService.save(user, jwtToken);
        userService.save(user);
        LoginResponse loginResponse = LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .token(jwtToken)
                .refreshToken(refreshToken.getRefreshToken())
                .tokenType("Bearer ")
                .build();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(loginResponse)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Successfully!")
                        .build()
        );
    }
}