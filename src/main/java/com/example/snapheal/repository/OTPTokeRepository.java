package com.example.snapheal.repository;

import com.example.snapheal.entities.OTPToken;
import com.example.snapheal.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OTPTokeRepository extends JpaRepository<OTPToken, Long> {
    @Query("SELECT t FROM OTPToken t WHERE t.token = :token AND t.user = :user AND t.isRevoked = false")
    Optional<OTPToken> findByTokenAndUserAndIsNotRevoked(@Param("token") String token, @Param("user") User user);

//    @Query("SELECT t FROM OTPToken t WHERE t.user.email = :email AND t.isRevoked = false")
//    List<OTPToken> findActiveTokensByUserEmail(@Param("email") String email);

    @Query("SELECT t FROM OTPToken t WHERE t.user.email = :email AND t.isRevoked = false")
    Optional<OTPToken> findActiveTokenByEmail(@Param("email") String email);
}
