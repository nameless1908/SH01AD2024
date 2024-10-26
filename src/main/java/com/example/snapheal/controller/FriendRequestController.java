package com.example.snapheal.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.entities.User;
import com.example.snapheal.repository.FriendRequestRepository;
import com.example.snapheal.responses.FriendRequestResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.FriendRequestService;

@RestController
@RequestMapping("/request")
public class FriendRequestController {

    @Autowired
    private FriendRequestService friendRequestService;

    // API để gửi yêu cầu kết bạn
    @PostMapping("")
    public ResponseEntity<ResponseObject> createFriendRequest(@RequestParam Long receiverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User requester = (User) authentication.getPrincipal();     
        User receiver = friendRequestService.findById(receiverId);

        FriendRequest friendRequest = friendRequestService.createFriendRequest(requester, receiver);
        return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.message("Created friend successfully")
        		.data(friendRequest)
        		.build()
        		);
    }
    
    // API để chấp nhận yêu cầu kết bạn
    @PutMapping("/accept")
    public ResponseEntity<ResponseObject> acceptFriendRequest(@RequestParam Long requestId) {
    	Optional<FriendRequest> friendRequestOpt = friendRequestService.acceptFriendRequest(requestId);

        return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.message("Friend request accepted successfully")
        		.data(friendRequestOpt)
        		.build()
        		);
    }
    
    // API để từ chối yêu cầu kết bạn
    @PutMapping("/reject")
    public ResponseEntity<ResponseObject> rejectFriendRequest(@RequestParam Long requestId) {
    	
    	Optional<FriendRequest> friendRequestOpt = friendRequestService.rejectFriendRequest(requestId);

        return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.message("Friend request rejected successfully")
        		.data(friendRequestOpt)
        		.build()
        		);
    }
    
    @GetMapping("/invite")
    public ResponseEntity<ResponseObject> getPendingFriendRequests() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        List<User> pendingRequests = friendRequestService.findPendingFriendRequests(userId);
        List<FriendRequestResponse> friendRequestResponses = pendingRequests.stream()
        		.map(FriendRequestResponse::fromUser)
        		.collect(Collectors.toList());
       
        	return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.message("Friend requests retrieved successfully")
        		.data(friendRequestResponses)
        		.build()
        		);
    }
}

