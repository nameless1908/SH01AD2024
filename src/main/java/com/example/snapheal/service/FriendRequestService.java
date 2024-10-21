package com.example.snapheal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.entities.Friend;
import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.entities.FriendStatus;
import com.example.snapheal.entities.User;
import com.example.snapheal.repository.FriendRepository;
import com.example.snapheal.repository.FriendRequestRepository;
import com.example.snapheal.repository.UserRepository;

@Service
public class FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendRepository friendRepository;

    // Tìm User dựa trên id
    public User findById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.orElse(null);
    }

    // Tìm yêu cầu kết bạn dựa trên requestId
    public Optional<FriendRequest> findByRequestId(Long requestId) {
        return friendRequestRepository.findById(requestId);
    }

    // Gửi yêu cầu kết bạn
    public FriendRequest createFriendRequest(User requester, User receiver) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequester(requester); // Người gửi yêu cầu kết bạn
        friendRequest.setReceiver(receiver); // Người nhận yêu cầu kết bạn
        friendRequest.setStatus(FriendStatus.PENDING); // Trạng thái ban đầu là PENDING

        return friendRequestRepository.save(friendRequest); // Lưu vào DB
    }

    // Chấp nhận yêu cầu kết bạn
    public Optional<FriendRequest> acceptFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findById(requestId);

        if (friendRequestOpt.isPresent()) {
            FriendRequest friendRequest = friendRequestOpt.get();
            friendRequest.setStatus(FriendStatus.ACCEPTED);  // Cập nhật trạng thái
            friendRequestRepository.save(friendRequest);  // Lưu vào database
            
            Friend friend = new Friend(friendRequest.getRequester(), friendRequest.getReceiver());
            friendRepository.save(friend);
        }
        return friendRequestOpt;
    }

    // Từ chối yêu cầu kết bạn
    public Optional<FriendRequest> rejectFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findById(requestId);

        if (friendRequestOpt.isPresent()) {
            FriendRequest friendRequest = friendRequestOpt.get();
            friendRequest.setStatus(FriendStatus.REJECTED);  // Cập nhật trạng thái
            friendRequestRepository.save(friendRequest);  // Lưu vào database
        }
        return friendRequestOpt;
    }
    
    // Lấy danh sách lời mời kết bạn
    public List<User> findPendingFriendRequests(Long userId) {
        return friendRequestRepository.findPendingFriendRequests(userId);
    }
}