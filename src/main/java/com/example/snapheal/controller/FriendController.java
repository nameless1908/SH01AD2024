package com.example.snapheal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.snapheal.model.User;
import com.example.snapheal.service.FriendService;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // API để lấy toàn bộ danh sách bạn bè của người dùng theo ID
    @GetMapping("")
    public ResponseEntity<List<User>> getAllFriends() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        // Lấy danh sách bạn bè từ service
        List<User> friends = friendService.getAllFriends(userId);
        
        // Trả về danh sách bạn bè nếu tìm thấy, nếu không trả về 404
        if (friends.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(friends);
    }

    // API để tìm kiếm bạn bè theo tên
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchFriends(@RequestParam String searchTerm) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        // Lấy danh sách bạn bè
        List<User> friends = friendService.searchFriends(userId, searchTerm);
        
        // Trả về danh sách nếu có, nếu không trả về 404
        if (friends.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(friends);
    }

    // API để xóa bạn bè
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable Long friendId) {
        // Lấy thông tin người dùng hiện tại từ SecurityContext
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        friendService.deleteFriend(userId, friendId);

        return ResponseEntity.noContent().build();
    }
}
