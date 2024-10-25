package com.example.snapheal.controller;

import com.example.snapheal.entities.Friend;
import com.example.snapheal.responses.FriendResponse;
import com.example.snapheal.responses.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.snapheal.entities.User;
import com.example.snapheal.service.FriendService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // API để lấy toàn bộ danh sách bạn bè của người dùng theo ID
    @GetMapping("/list")
    public ResponseEntity<ResponseObject> getAllFriends() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        
        List<FriendResponse> friends = friendService.getAllFriends(userId);
        
        // Trả về danh sách bạn bè nếu tìm thấy, nếu không trả về 404
//        if (friends.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
        
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Successfully!")
                        .data(friends)
                        .build()
        );
    }

    // API để tìm kiếm bạn bè theo tên
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchFriends(@RequestParam String searchTerm) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        
        List<User> friends = friendService.searchFriends(userId, searchTerm);
        
        // Trả về danh sách nếu có, nếu không trả về 404
        if (friends.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(friends);
    }

    // API để xóa bạn bè
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFriend(@RequestParam Long friendId) {
        
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        friendService.deleteFriend(userId, friendId);

        return ResponseEntity.noContent().build();
    }
}
