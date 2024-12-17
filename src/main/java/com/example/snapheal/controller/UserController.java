package com.example.snapheal.controller;

import java.io.IOException;
import java.util.List;

import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.responses.*;
import com.example.snapheal.service.JwtService;
import com.example.snapheal.service.RefreshTokenService;
import com.example.snapheal.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.snapheal.dtos.UpdateCurrentLocationDto;
import com.example.snapheal.dtos.UpdateUserDto;
import com.example.snapheal.entities.User;
import com.example.snapheal.service.UserService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private UploadService uploadService;
	@Autowired
	private RefreshTokenService refreshTokenService;
	@Autowired
	private JwtService jwtService;

	@GetMapping("/user-login")
	public ResponseEntity<ResponseObject> getUserLogin(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        List<ProfileResponse> profileResponses = userService.getProfileUser(userId);

		return ResponseEntity.ok(
				ResponseObject.builder()
				.status(HttpStatus.OK)
				.code(HttpStatus.OK.value())
				.message("Get infomation user login successfully")
				.data(profileResponses)
				.build()
				);
	}

	
	@GetMapping("/search")
	public ResponseEntity<ResponseObject> searchUsers(@RequestParam String searchTerm) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

	    List<UserResponse> userResponses = userService.searchUserWithFriendRequestStatus(userId, searchTerm);

	    return ResponseEntity.ok(
	    		ResponseObject.builder()
	    		.status(HttpStatus.OK)
	    		.code(HttpStatus.OK.value())
	    		.message("Search user successfully")
	    		.data(userResponses)
	    		.build());
	}

	//Cập nhật User
	@PutMapping("/update")
	public ResponseEntity<ResponseObject> updateUser(@RequestBody UpdateUserDto dto, HttpServletRequest request) {
	    User user = userService.updateUser(dto);
		String authHeader = request.getHeader("Authorization");
		final String jwt = authHeader.substring(7);
		RefreshToken token = refreshTokenService.findByToken(jwt);
		LoginResponse loginResponse = LoginResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.username(user.getUsername())
				.fullName(user.getFullName())
				.avatar(user.getAvatar())
				.token(jwt)
				.refreshToken(token.getRefreshToken())
				.tokenType("Bearer ")
				.build();
	    return ResponseEntity.ok(
	            ResponseObject.builder()
	                    .data(loginResponse)
	                    .status(HttpStatus.OK)
	                    .code(HttpStatus.OK.value())
	                    .message("User updated successfully!")
	                    .build()
	    );
	}

	@GetMapping("/detail")
	public ResponseEntity<ResponseObject> getDetail(@RequestParam Long id) {
		UserResponse response = userService.getDetail(id);
		return ResponseEntity.ok(
				ResponseObject.builder()
						.data(response)
						.status(HttpStatus.OK)
						.code(HttpStatus.OK.value())
						.message("User updated successfully!")
						.build()
		);
	}
	
	@GetMapping("/nearby")
	public ResponseEntity<ResponseObject> getNearbyUsers() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    User currentUser = (User) authentication.getPrincipal();
	    Long currentUserId = currentUser.getId();

	    List<UserDistanceResponse> nearbyUsers = userService.findNearbyUsers(currentUserId);

	    return ResponseEntity.ok(
	            ResponseObject.builder()
	                    .status(HttpStatus.OK)
	                    .code(HttpStatus.OK.value())
	                    .message("Nearby users retrieved successfully")
	                    .data(nearbyUsers)
	                    .build()
	    );
	}
	
	@PutMapping("/update-location")
	public ResponseEntity<ResponseObject> updateCurrentLocation(@RequestBody UpdateCurrentLocationDto dto) {
	    userService.updateLocation(dto);
	    return ResponseEntity.ok(
	            ResponseObject.builder()
	                    .data(true)
	                    .status(HttpStatus.OK)
	                    .code(HttpStatus.OK.value())
	                    .message("Current location updated successfully!")
	                    .build()
	    );
	}

	@PutMapping("/update-avatar/{id}")
	public ResponseEntity<ResponseObject> updateAvatar(@PathVariable Long id,
													   @RequestParam MultipartFile file) throws IOException {
		String url = uploadService.uploadImage(file);
		userService.updateAvatar(id, url);
		return ResponseEntity.ok(
				ResponseObject.builder()
						.data(url)
						.status(HttpStatus.OK)
						.code(HttpStatus.OK.value())
						.message("Avatar updated successfully!")
						.build()
		);
	}
}
