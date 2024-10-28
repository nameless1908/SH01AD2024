package com.example.snapheal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.snapheal.entities.User;
import com.example.snapheal.responses.ProfileResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.responses.UserResponse;
import com.example.snapheal.service.UserService;

@RestController
@RequestMapping("/user")
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

	    List<Object[]> results = userService.searchUserWithFriendRequestStatus(userId, searchTerm);
	    List<UserResponse> userResponses = results.stream()
	            .map(result -> {
	                // Tạo UserResponse trực tiếp từ dữ liệu kết quả
	                return new UserResponse(
	                    (String) result[7], // username
	                    (String) result[4], // fullname
	                    (String) result[1], // avatar
	                    (String) result[8]  // status 
	                );
	            })
	            .collect(Collectors.toList());
		
	    return ResponseEntity.ok(
	    		ResponseObject.builder()
	    		.status(HttpStatus.OK)
	    		.message("Search user successfully")
	    		.data(userResponses)
	    		.build());
	}
}
