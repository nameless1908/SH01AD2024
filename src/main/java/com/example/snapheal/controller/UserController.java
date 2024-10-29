package com.example.snapheal.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.snapheal.dtos.UpdateUserDto;
import com.example.snapheal.entities.User;
import com.example.snapheal.responses.ProfileResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.responses.UserDistanceResponse;
import com.example.snapheal.responses.UserResponse;
import com.example.snapheal.service.UserService;

@RestController
@RequestMapping("${api.prefix}/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	//lấy thông tin người dùng login
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
	
	//tìm kiếm người dùng
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
	public ResponseEntity<ResponseObject> updateUser(@RequestBody UpdateUserDto dto) {
	    userService.updateUser(dto);
	    return ResponseEntity.ok(
	            ResponseObject.builder()
	                    .data(true)
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
}
