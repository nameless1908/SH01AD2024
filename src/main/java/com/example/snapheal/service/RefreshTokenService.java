package com.example.snapheal.service;

import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.entities.User;
import com.example.snapheal.exceptions.TokenInvalidException;
import com.example.snapheal.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Value("${security.jwt.expiration-time}")
    private long tokenExpirationTime;

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshTokenExpirationTime;

    public void save(RefreshToken token) {
        refreshTokenRepository.save(token);
    }

    public RefreshToken save(User user, String token) {
        Date now = new Date();
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenType("Bearer")
                .token(token)
                .refreshToken(UUID.randomUUID().toString())
                .user(user)
                .revoked(false)
                .tokenExpirationDate(LocalDateTime.now().plusSeconds(tokenExpirationTime))
                .refreshTokenExpirationDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationTime))
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                () -> new TokenInvalidException("Refresh token not found!")
        );
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElseThrow(
                () -> new TokenInvalidException("Not found RefreshToken with Token!")
        );
    }

    public RefreshToken refreshToken(User user) {
        RefreshToken newToken = RefreshToken.builder()
                .token(jwtService.generateToken(user))
                .tokenExpirationDate(LocalDateTime.now().plusSeconds(tokenExpirationTime))
                .refreshToken(UUID.randomUUID().toString())
                .refreshTokenExpirationDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationTime))
                .revoked(false)
                .tokenType("Bearer")
                .user(user)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        return refreshTokenRepository.save(newToken);
    }
}