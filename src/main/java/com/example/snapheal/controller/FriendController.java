package com.example.snapheal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.snapheal.entities.User;
import com.example.snapheal.responses.FriendResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.FriendService;

import jakarta.persistence.criteria.From;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // API để lấy toàn bộ danh sách bạn bè của người dùng theo ID
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllFriends() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        List<User> friends = friendService.getAllFriends(userId);
        List<FriendResponse> friendResponses = friends.stream()
                .map(FriendResponse::fromUser)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Get Friend successfully")
                            .data(friendResponses)  
                            .build()
            ); 
    }
    
    // API để tìm kiếm bạn bè theo tên
    @GetMapping("/search")
    public ResponseEntity<ResponseObject> searchFriends(@RequestParam String searchTerm) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        
        List<User> friends = friendService.searchFriends(userId, searchTerm);
        List<FriendResponse> friendResponses = friends.stream()
        		.map(FriendResponse::fromUser)
        		.collect(Collectors.toList());
		
        return ResponseEntity.ok(
        		ResponseObject.builder()
        			.status(HttpStatus.OK)
        			.message("Find friend successfully")
        			.data(friendResponses)
        			.build()
        		);
    }

    // API để xóa bạn bè
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseObject> deleteFriend(@RequestParam Long friendId) {
        
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        friendService.deleteFriend(userId, friendId);

        return ResponseEntity.ok(
        		ResponseObject.builder()
        			.status(HttpStatus.NO_CONTENT)
        			.message("Friend deleted successfully")
        			.data(null)
        			.build()
        			);
    }
}
