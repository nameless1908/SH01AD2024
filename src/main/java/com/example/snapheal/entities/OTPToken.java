package com.example.snapheal.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPToken {
    private static final int EXPIRATION = 60 * 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private LocalDateTime expireDate;
    private Boolean isRevoked;
    private String verifyToken;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public boolean isExpiredToken() {
        return expireDate.isBefore(LocalDateTime.now());
    }
}
