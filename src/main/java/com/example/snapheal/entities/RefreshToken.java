package com.example.snapheal.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenType;
    private String token;

    @UpdateTimestamp
    @Column(columnDefinition = "timestamp")
    private LocalDateTime tokenExpirationDate;

    private String refreshToken;

    @UpdateTimestamp
    @Column(columnDefinition = "timestamp")
    private LocalDateTime refreshTokenExpirationDate;
    private boolean revoked;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at", columnDefinition = "timestamp")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at", columnDefinition = "timestamp")
    private LocalDateTime updateAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
