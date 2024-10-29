package com.example.snapheal.controller;

import java.util.List;
import java.util.Optional;

import com.example.snapheal.dtos.SendFriendRequestDto;
import com.example.snapheal.dtos.UpdateFriendRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.entities.User;
import com.example.snapheal.responses.FriendRequestResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.FriendRequestService;

@RestController
@RequestMapping("${api.prefix}/friend-request")
public class FriendRequestController {

    @Autowired
    private FriendRequestService friendRequestService;

    // API để gửi yêu cầu kết bạn
    @PostMapping("/send")
    public ResponseEntity<ResponseObject> createFriendRequest(@RequestBody SendFriendRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User requester = (User) authentication.getPrincipal();     
        User receiver = friendRequestService.findById(dto.getReceiverId());

        FriendRequest friendRequest = friendRequestService.createFriendRequest(requester, receiver);
        
        return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.code(HttpStatus.OK.value())
        		.message("Created friend successfully")
        		.data(true)
        		.build()
        		);
    }
    
    // API để chấp nhận yêu cầu kết bạn
    @PutMapping("/accept")
    public ResponseEntity<ResponseObject> acceptFriendRequest(@RequestBody UpdateFriendRequestDto dto) {
    	friendRequestService.acceptFriendRequest(dto.getRequesterId());

        return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.code(HttpStatus.OK.value())
        		.message("Friend request accepted successfully")
        		.data(true)
        		.build()
        		);
    }
    
    // API để từ chối yêu cầu kết bạn
    @PutMapping("/reject")
    public ResponseEntity<ResponseObject> rejectFriendRequest(@RequestBody UpdateFriendRequestDto dto) {
    	
    	friendRequestService.rejectFriendRequest(dto.getRequesterId());

        return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.code(HttpStatus.OK.value())
        		.message("Friend request rejected successfully")
        		.data(true)
        		.build()
        		);
    }
    
    @GetMapping("/list")
    public ResponseEntity<ResponseObject> getPendingFriendRequests() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        
        List<FriendRequestResponse> friendRequestResponses = friendRequestService.findFriendRequests(userId);
       
        	return ResponseEntity.ok(
        		ResponseObject.builder()
        		.status(HttpStatus.OK)
        		.code(HttpStatus.OK.value())
        		.message("Friend requests retrieved successfully")
        		.data(friendRequestResponses)
        		.build()
        		);
    }
}

