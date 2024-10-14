package com.example.snapheal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.snapheal.model.User;
import com.example.snapheal.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	// API tìm kiếm người dùng
	@GetMapping("/search")
	public ResponseEntity<List<User>> searchUsers(@RequestParam String searchTerm){
		List<User> users = userService.searchUser(searchTerm);
		// Trả về danh sách nếu có, nếu không trả về 404
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(users);
	}
}
