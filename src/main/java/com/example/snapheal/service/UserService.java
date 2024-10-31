package com.example.snapheal.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.snapheal.dtos.UpdateCurrentLocationDto;
import com.example.snapheal.dtos.UpdateUserDto;
import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.entities.FriendStatus;
import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.entities.Status;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.exceptions.TokenInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.snapheal.entities.User;
import com.example.snapheal.repository.UserRepository;
import com.example.snapheal.responses.ProfileResponse;
import com.example.snapheal.responses.UserDistanceResponse;
import com.example.snapheal.responses.UserResponse;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RefreshTokenService refreshTokenService;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private FriendRequestService friendRequestService;

	public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
	
	public List<UserResponse> searchUserWithFriendRequestStatus(Long currentUserId, String searchTerm) {
	    List<Object[]> list = userRepository.searchUsersWithFriendStatus(currentUserId, searchTerm);
	    List<UserResponse> userResponses = list.stream()
	        .map(result -> {
	            Long id = (Long) result[0];
	            String username = (String) result[9];
	            String fullName = (String) result[6];
	            String avatar = (String) result[1];
	            String statusString = (String) result[10];

	            Status status = Status.valueOf(statusString.toUpperCase());

	            return UserResponse.builder()
	                .id(id)
	                .username(username)
	                .fullName(fullName)
	                .avatar(avatar)
	                .status(status)
	                .build();
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

	public UserResponse getDetail(Long id) {
		User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User targetUser = userRepository.findById(id).orElseThrow(() -> new CustomErrorException("Not found user by id" + id));

		Optional<FriendRequest> friendRequest = friendRequestService.getFriendRequestByUserIds(userDetails.getId(), id);
		Status status;

		if (friendRequest.isPresent()) {
		    if (friendRequest.get().getStatus() == FriendStatus.PENDING) {
		        if (Objects.equals(friendRequest.get().getRequester().getId(), userDetails.getId())) {
		            status = Status.SENDING;  
		        } else {
		            status = Status.PENDING;  
		        }
		    } else {
		    	status = mapFriendStatusToStatus(friendRequest.get().getStatus(), friendRequest.get().getRequester().getId(), userDetails.getId());  
		    }
		} else {
		    status = Status.NONE;  
		}

		return UserResponse.builder()
				.id(targetUser.getId())
				.username(targetUser.getUsername())
				.fullName(targetUser.getFullName())
				.status(status)
				.avatar(targetUser.getAvatar())
				.build();
	}
	
	private Status mapFriendStatusToStatus(FriendStatus friendStatus, Long requesterId, Long currentUserId) {
	    if (friendStatus == FriendStatus.PENDING) {
	        return requesterId.equals(currentUserId) ? Status.SENDING : Status.PENDING;
	    }
	    if (friendStatus == FriendStatus.ACCEPTED) {
	        return Status.ACCEPTED;
	    }
	    if (friendStatus == FriendStatus.REJECTED) {
	        return Status.REJECTED;
	    }
	    return Status.NONE;
	}

	
	public List<UserDistanceResponse> findNearbyUsers(Long currentUserId) {
	    User currentUser = userRepository.findById(currentUserId)
	            .orElseThrow(() -> new CustomErrorException("User not found"));

	    double currentLat = currentUser.getCurrentLatitude();
	    double currentLng = currentUser.getCurrentLongitude();

	    List<User> potentialFriends = userRepository.findUsersExcludingFriendsAndPendingRequests(currentUserId);

	    return potentialFriends.stream()
	            .map(user -> {
	                double distance = distanceBetween2Points(currentLat, currentLng,
	                        user.getCurrentLatitude(), user.getCurrentLongitude());
	                return new UserDistanceResponse(user.getId(), user.getUsername(), user.getFullName(),
	                        user.getAvatar(), distance);
	            })
	            .sorted(Comparator.comparingDouble(UserDistanceResponse::getDistance))
	            .limit(5)
	            .collect(Collectors.toList());
	}

	public static double distanceBetween2Points(double la1, double lo1, double la2, double lo2) {
	    double R = 6371;
	    double dLat = (la2 - la1) * (Math.PI / 180);
	    double dLon = (lo2 - lo1) * (Math.PI / 180);
	    double la1ToRad = la1 * (Math.PI / 180);
	    double la2ToRad = la2 * (Math.PI / 180);
	    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(la1ToRad)
	                * Math.cos(la2ToRad) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double d = R * c;
	    return d;
	}
	
	public void updateLocation(UpdateCurrentLocationDto dto) {
		 User user = userRepository.findById(dto.getId()).orElseThrow(
		            () -> new CustomErrorException("Can not found User with id: " + dto.getId())
		    );
		 
		 user.setCurrentLatitude(dto.getCurrentLatitude());
		 user.setCurrentLongitude(dto.getCurrentLongitude());
		 
		 userRepository.save(user);	 
	}
}
