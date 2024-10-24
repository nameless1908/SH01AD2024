package com.example.snapheal.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
@Entity // Ensure this annotation is present
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tokenType;
    private String token;
    private LocalDateTime tokenExpirationDate;

    private String refreshToken;
    private LocalDateTime refreshTokenExpirationDate;
    private boolean revoked;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createAt;

    @CreationTimestamp
    @Column(updatable = true, name = "update_at")
    private Date updateAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
