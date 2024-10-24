package com.example.snapheal.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.exceptions.TokenInvalidException;
import com.example.snapheal.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.entities.User;
import com.example.snapheal.repository.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RefreshTokenService refreshTokenService;
	@Autowired
	private JwtService jwtService;

	public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
	
	public List<Object[]> searchUserWithFriendRequestStatus(Long currentUserId, String searchTerm) {
	    return userRepository.searchUsersWithFriendStatus(currentUserId, searchTerm);
	}

	public User getUserDetailsFromToken(String token) throws Exception {
		String username = jwtService.extractUsername(token);
		return  userRepository.findByUsername(username).orElseThrow(
				() -> new TokenInvalidException("User not Found with username")
		);
	}

	public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
		RefreshToken existToken = refreshTokenService.findByRefreshToken(refreshToken);
		if (existToken.isRevoked()) {
			throw  new TokenInvalidException("Refresh token has already been revoked!");
		}
		if (existToken.getRefreshTokenExpirationDate().isBefore(LocalDateTime.now())) {
			throw new TokenInvalidException("Refresh Token Expired!");
		}
		return getUserDetailsFromToken(existToken.getToken());
	}
}
