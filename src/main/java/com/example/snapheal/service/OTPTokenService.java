package com.example.snapheal.service;

import com.example.snapheal.Utils.TokenGenerator;
import com.example.snapheal.entities.OTPToken;
import com.example.snapheal.entities.User;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.repository.OTPTokeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OTPTokenService {
    @Autowired
    private OTPTokeRepository otpTokeRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public OTPToken createNewOTPToken(String email) throws Exception {
        User user = userService.findUserByEmail(email).orElseThrow(
                () -> new CustomErrorException("Email not registered!")
        );

        Optional<OTPToken> activeToken = otpTokeRepository.findActiveTokenByEmail(email);
        if (activeToken.isPresent()) {
            activeToken.get().setIsRevoked(true);
            otpTokeRepository.save(activeToken.get());
        }

        String token = TokenGenerator.generate6DigitToken();
        OTPToken newToken = OTPToken.builder()
                .token(token)
                .user(user)
                .isRevoked(false)
                .verifyToken("")
                .expireDate(LocalDateTime.now().plusMinutes(TokenGenerator.EXPIRATION_MINUTE))
                .build();
        otpTokeRepository.save(newToken);
        return newToken;
    }

    public String validateToken(String email, String token) {
        User user = userService.findUserByEmail(email).orElseThrow(
                () -> new CustomErrorException("Email not registered!")
        );

        OTPToken otpToken = otpTokeRepository.findByTokenAndUserAndIsNotRevoked(token, user).orElseThrow(
                () -> new CustomErrorException("Token Invalid!")
        );

        if (otpToken.isExpiredToken()) {
            throw new CustomErrorException("Token Expired!");
        }

        String verifyToken = UUID.randomUUID().toString();
        otpToken.setVerifyToken(passwordEncoder.encode(verifyToken));
        otpTokeRepository.save(otpToken);
        return verifyToken;
    }

    public Boolean validateVerifyOTPToken(String email, String verifyToken) {
        OTPToken otpToken = otpTokeRepository.findActiveTokenByEmail(email).orElseThrow(
                () -> new CustomErrorException("Not found active OTPToken by email!")
        );
        return passwordEncoder.matches(verifyToken, otpToken.getVerifyToken());
    }
}
