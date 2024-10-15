package com.example.snapheal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.snapheal.model.FriendRequest;
import com.example.snapheal.model.User;
import com.example.snapheal.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	// API tìm kiếm người dùng
	@GetMapping("/search")
	public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String searchTerm) {
	    
	    Long userId = 1L;

	    List<Object[]> results = userService.searchUserWithFriendRequestStatus(userId, searchTerm);

	    if (results.isEmpty()) {
	        return ResponseEntity.notFound().build();
	    }

	    List<Map<String, Object>> usersWithFriendStatus = new ArrayList<>();
	    
	    for (Object[] result : results) {
	    	Map<String, Object> user = new HashMap<>();
	        
	        // Ánh xạ thông tin user từ mảng kết quả SQL
	        user.put("id", result[0]); // userId
	        user.put("email", result[1]); // email
	        user.put("username", result[2]); // username
	        user.put("password", result[3]); // password
	        user.put("avatar", result[4]); // avatar
	        user.put("createdAt", result[5]); // createdAt
	        user.put("updatedAt", result[6]); // updatedAt
	        user.put("friendStatus", result[7]);

	        // Trạng thái kết bạn
	        Map<String, Object> userWithStatus = new HashMap<>();
	        userWithStatus.put("user", user);
	        usersWithFriendStatus.add(userWithStatus);
	    }
		return ResponseEntity.ok(usersWithFriendStatus);
	}
}
