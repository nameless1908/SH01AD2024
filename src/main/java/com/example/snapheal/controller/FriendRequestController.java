package com.example.snapheal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.snapheal.model.FriendRequest;
import com.example.snapheal.model.FriendStatus;
import com.example.snapheal.model.User;
import com.example.snapheal.repository.FriendRequestRepository;
import com.example.snapheal.service.FriendRequestService;

@RestController
@RequestMapping("/request")
public class FriendRequestController {

    @Autowired
    private FriendRequestService friendRequestService;
    
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    // API để gửi yêu cầu kết bạn
    @PostMapping("")
    public ResponseEntity<FriendRequest> createFriendRequest(@RequestParam Long receiverId) {
        // Lấy thông tin người dùng hiện tại từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User requester = (User) authentication.getPrincipal();

        // Tìm người nhận dựa trên ID
        User receiver = friendRequestService.findById(receiverId);
        
        if (requester == null || receiver == null) {
            return ResponseEntity.badRequest().build();  // Nếu người dùng không tồn tại
        }

        // Gửi yêu cầu kết bạn
        FriendRequest friendRequest = friendRequestService.createFriendRequest(requester, receiver);
        return ResponseEntity.ok(friendRequest);
    }
    
    // API để chấp nhận yêu cầu kết bạn
    @PutMapping("/accept")
    public ResponseEntity<FriendRequest> acceptFriendRequest(@RequestParam Long requestId) {
    	Optional<FriendRequest> friendRequestOpt = friendRequestService.acceptFriendRequest(requestId);

        return friendRequestOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // API để từ chối yêu cầu kết bạn
    @PutMapping("/reject")
    public ResponseEntity<FriendRequest> rejectFriendRequest(@RequestParam Long requestId) {
    	Optional<FriendRequest> friendRequestOpt = friendRequestService.rejectFriendRequest(requestId);

        return friendRequestOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/invite")
    public ResponseEntity<List<User>> getPendingFriendRequests(@RequestParam Long userId) {
        List<User> pendingRequests = friendRequestService.findPendingFriendRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }
}

