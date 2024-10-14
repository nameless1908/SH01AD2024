package com.example.snapheal.controller;

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
    @PutMapping("/{requestId}/accept")
    public ResponseEntity<FriendRequest> acceptFriendRequest(@PathVariable Long requestId) {
        Optional<FriendRequest> friendRequestOpt = friendRequestService.findByRequestId(requestId);
        
        if (friendRequestOpt.isPresent()) {
            FriendRequest friendRequest = friendRequestOpt.get();
            friendRequest.setStatus(FriendStatus.ACCEPTED);  // Cập nhật trạng thái thành ACCEPTED
            friendRequestRepository.save(friendRequest);
            return ResponseEntity.ok(friendRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // API để từ chối yêu cầu kết bạn
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<FriendRequest> rejectFriendRequest(@PathVariable Long requestId) {
        Optional<FriendRequest> friendRequestOpt = friendRequestService.findByRequestId(requestId);
        
        if (friendRequestOpt.isPresent()) {
            FriendRequest friendRequest = friendRequestOpt.get();
            friendRequest.setStatus(FriendStatus.REJECTED);  // Cập nhật trạng thái thành REJECTED
            friendRequestRepository.save(friendRequest);
            return ResponseEntity.ok(friendRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

