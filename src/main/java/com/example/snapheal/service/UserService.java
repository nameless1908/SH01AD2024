package com.example.snapheal.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.snapheal.dtos.UpdateUserDto;
import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.exceptions.TokenInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.entities.User;
import com.example.snapheal.repository.UserRepository;
import com.example.snapheal.responses.ProfileResponse;
import com.example.snapheal.responses.UserResponse;

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
	
	public List<UserResponse> searchUserWithFriendRequestStatus(Long currentUserId, String searchTerm) {
	    List<Object[]> list = userRepository.searchUsersWithFriendStatus(currentUserId, searchTerm);
	    List<UserResponse> userResponses = list.stream()
	            .map(result -> {
	                return new UserResponse(
	                	(Long) result[0],   // id
	                    (String) result[9], // username
	                    (String) result[6], // fullname
	                    (String) result[1], // avatar
	                    (String) result[10]  // status 
	                );
	            })
	            .collect(Collectors.toList());
	    return userResponses;
	}
	
	public List<ProfileResponse> getProfileUser(Long userId){
		Optional<User> users = userRepository.findById(userId);
		List<ProfileResponse> profileResponses = users.stream()
				.map(User::mapToProfileResponse).toList();
		
		return profileResponses;
	}
	
	public void updateUser(UpdateUserDto dto) {
	    User user = userRepository.findById(dto.getId()).orElseThrow(
	            () -> new CustomErrorException("Can not found User with id: " + dto.getId())
	    );

	    
	    if (!user.getUsername().equals(dto.getUsername())) {
	        Optional<User> existingUser = userRepository.findByUsername(dto.getUsername());
	        if (existingUser.isPresent()) {
	            throw new CustomErrorException("Username already exists");
	        }
	        user.setUsername(dto.getUsername());
	    }
	    
	    if (user.getEmail().equals(dto.getEmail())) {
            throw new CustomErrorException("Email exist");
        }

	    user.setFullName(dto.getFullname());
	    user.setEmail(dto.getEmail());
	    user.setAvatar(dto.getAvatar());

	    
	    userRepository.save(user);
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
		if (existToken.getTokenExpirationDate().isBefore(LocalDateTime.now())) {
			existToken.setRevoked(true);
			existToken.setUpdateAt(new Date());
			refreshTokenService.save(existToken);
		}
		return existToken.getUser();
	}
}
