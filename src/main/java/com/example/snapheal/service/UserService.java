package com.example.snapheal.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.snapheal.dtos.UpdateCurrentLocationDto;
import com.example.snapheal.dtos.UpdateUserDto;
import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.entities.UserLocation;
import com.example.snapheal.enums.FriendStatus;
import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.enums.Status;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.exceptions.TokenInvalidException;
import com.example.snapheal.repository.UserLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.snapheal.entities.User;
import com.example.snapheal.repository.UserRepository;
import com.example.snapheal.responses.ProfileResponse;
import com.example.snapheal.responses.UserDistanceResponse;
import com.example.snapheal.responses.UserResponse;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserLocationRepository userLocationRepository;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private FriendRequestService friendRequestService;

	public void save(User user) {
		userRepository.save(user);
	}
	public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
	public Optional<User> findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	public List<UserResponse> searchUserWithFriendRequestStatus(Long currentUserId, String searchTerm) {
	    List<Object[]> list = userRepository.searchUsersWithFriendStatus(currentUserId, searchTerm);

        return list.stream()
            .map(result -> {
                Long id = (Long) result[0];
                String username = (String) result[7];
                String fullName = (String) result[4];
                String avatar = (String) result[1];
                String statusString = (String) result[8];

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
	    
	}
	
	public List<ProfileResponse> getProfileUser(Long userId){
		Optional<User> users = userRepository.findById(userId);

        return users.stream()
                .map(User::mapToProfileResponse).toList();
	}
	
	public User updateUser(UpdateUserDto dto) {
	    User user = userRepository.findById(dto.getId()).orElseThrow(
	            () -> new CustomErrorException("Can not found User with id: " + dto.getId())
	    );
		user.setUsername(dto.getUsername());
	    user.setFullName(dto.getFullname());
	    user.setEmail(dto.getEmail());
	    user.setAvatar(dto.getAvatar());

	    return userRepository.save(user);
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
			existToken.setUpdatedAt(LocalDateTime.now());
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
//	    User currentUser = userRepository.findById(currentUserId)
//	            .orElseThrow(() -> new CustomErrorException("User not found"));
		Optional<UserLocation> userLocation = userLocationRepository.findByUserId(currentUserId);

		if (userLocation.isEmpty()) {
			return List.of();
		}
		double currentLat = userLocation.get().getCurrentLatitude();
		double currentLng = userLocation.get().getCurrentLongitude();
		List<User> potentialFriends = userRepository.findUsersExcludingFriendsAndPendingRequests(currentUserId);

		List<UserDistanceResponse> userDistanceResponses = new ArrayList<>();

		for(User user: potentialFriends) {
			Optional<UserLocation> location = userLocationRepository.findByUserId(user.getId());
			if (location.isEmpty()) {
				break;
			}
			double distance = distanceBetween2Points(currentLat, currentLng,
					location.get().getCurrentLatitude(), location.get().getCurrentLongitude());
			userDistanceResponses.add(new UserDistanceResponse(user.getId(), user.getUsername(), user.getFullName(),
					user.getAvatar(), distance));
		}

		return userDistanceResponses.stream()
				.sorted(Comparator.comparingDouble(UserDistanceResponse::getDistance))
				.limit(20)
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
        return R * c;
	}
	
	public void updateLocation(UpdateCurrentLocationDto dto) {
		 User user = userRepository.findById(dto.getId()).orElseThrow(
		            () -> new CustomErrorException("Can not found User with id: " + dto.getId())
		    );

		Optional<UserLocation> userLocation = userLocationRepository.findByUserId(dto.getId());
		if (userLocation.isPresent()) {
			userLocation.get().setCurrentLatitude(dto.getCurrentLatitude());
			userLocation.get().setCurrentLongitude(dto.getCurrentLongitude());

			userLocationRepository.save(userLocation.get());
		} else {
			UserLocation newUserLocation = new UserLocation();
			newUserLocation.setUser(user);
			newUserLocation.setCurrentLongitude(dto.getCurrentLongitude());
			newUserLocation.setCurrentLatitude(dto.getCurrentLatitude());
			userLocationRepository.save(newUserLocation);
		}
	}

	public void updateAvatar(Long id, String avatar) {
		User user = userRepository.findById(id).orElseThrow(
				() -> new CustomErrorException("Can not found User with id: " + id)
		);
		user.setAvatar(avatar);
		userRepository.save(user);
	}
}
