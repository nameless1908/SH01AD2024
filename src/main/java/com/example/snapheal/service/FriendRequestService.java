package com.example.snapheal.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.model.FriendRequest;
import com.example.snapheal.model.FriendStatus;
import com.example.snapheal.model.User;
import com.example.snapheal.repository.FriendRequestRepository;
import com.example.snapheal.repository.UserRepository;

@Service
public class FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

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

    public void deleteFriendRequest(FriendRequest friendRequest) {
        friendRequestRepository.delete(friendRequest); // Xóa yêu cầu kết bạn
    }
}