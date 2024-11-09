package com.example.snapheal.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.example.snapheal.exceptions.CustomErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.snapheal.entities.Friend;
import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.entities.FriendStatus;
import com.example.snapheal.entities.User;
import com.example.snapheal.repository.FriendRepository;
import com.example.snapheal.repository.FriendRequestRepository;
import com.example.snapheal.repository.UserRepository;
import com.example.snapheal.responses.FriendRequestResponse;

@Service
public class FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendRepository friendRepository;

    
    public User findById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.orElse(null);
    }


    public Optional<FriendRequest> findByRequestId(Long requestId) {
        return friendRequestRepository.findById(requestId);
    }

    
    public FriendRequest createFriendRequest(User requester, User receiver) {
        
    	Long requesterId = requester.getId();
    	Long receiverId = receiver.getId();
    	
        if (Objects.equals(requesterId, receiverId)) {
            throw new CustomErrorException("Cannot send a friend request to yourself.");
        }

        Optional<Friend> existingFriendship = friendRepository.findByUserAndFriend(requesterId, receiverId);
        if (existingFriendship.isPresent()) {
            throw new CustomErrorException("You are already friends with this user.");
        }

        Optional<FriendRequest> existingRequest = friendRequestRepository
                .findByRequesterAndReceiverAndStatus(requesterId, receiverId, FriendStatus.PENDING);
        if (existingRequest.isPresent()) {
            throw new CustomErrorException("A friend request has already been sent.");
        }

        Optional<FriendRequest> reverseRequest = friendRequestRepository
                .findByRequesterAndReceiverAndStatus(receiverId, requesterId, FriendStatus.PENDING);
        if (reverseRequest.isPresent()) {
            throw new CustomErrorException("The recipient has already sent a friend request.");
        }

        Optional<FriendRequest> existingRejectedRequest = friendRequestRepository
                .findByRequesterAndReceiverAndStatus(requesterId, receiverId, FriendStatus.REJECTED);
        if (existingRejectedRequest.isPresent()) {
            FriendRequest rejectedRequest = existingRejectedRequest.get();
            rejectedRequest.setStatus(FriendStatus.PENDING); 
            return friendRequestRepository.save(rejectedRequest);
        }

        Optional<FriendRequest> requesterReject = friendRequestRepository
                .findByRequesterAndReceiverAndStatus(receiverId, requesterId, FriendStatus.REJECTED);
        if (requesterReject.isPresent()) {
            FriendRequest rejectedRequest = requesterReject.get();
            rejectedRequest.setStatus(FriendStatus.PENDING);
            rejectedRequest.setRequester(requester);
            rejectedRequest.setReceiver(receiver);
            return friendRequestRepository.save(rejectedRequest);
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequester(requester);
        friendRequest.setReceiver(receiver);
        friendRequest.setStatus(FriendStatus.PENDING);

        return friendRequestRepository.save(friendRequest);
    }
    
    public void acceptFriendRequest(Long requesterId) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        FriendRequest friendRequest = friendRequestRepository.findRequestByRequesterId(userDetails.getId(), requesterId).orElseThrow(
                () -> new CustomErrorException("Not found requester by requestID")
        );
        friendRequest.setStatus(FriendStatus.ACCEPTED);  
        friendRequestRepository.save(friendRequest);  

        Friend friend = new Friend(friendRequest.getRequester(), friendRequest.getReceiver());
        friendRepository.save(friend);
    }

    public void rejectFriendRequest(Long requesterId) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        FriendRequest friendRequest = friendRequestRepository.findRequestByRequesterId(userDetails.getId(), requesterId).orElseThrow(
                () -> new CustomErrorException("Not found requester by requestID")
        );
        friendRequest.setStatus(FriendStatus.REJECTED);  
        friendRequestRepository.save(friendRequest);  
    }
    
    public List<FriendRequestResponse> findFriendRequests(Long userId) {
        List<User> users = friendRequestRepository.findPendingFriendRequests(userId);
        return users.stream()
                    .map(user -> user.mapToFriendRequestResponse(FriendStatus.PENDING))
                    .toList();
    }
    
    public Optional<FriendRequest> getFriendRequestByUserIds(Long userId, Long targetId) {
        return friendRequestRepository.findFriendRequestByUserIds(userId, targetId);
    }
    
    public void cancelFriendRequest(User requester, User receiver) {
    	Long requesterId = requester.getId();
    	Long receiverId = receiver.getId();
    	
        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findPendingRequestByUserIds(requesterId, receiverId);
        
        if (friendRequestOpt.isPresent()) {
            FriendRequest friendRequest = friendRequestOpt.get();
            if (friendRequest.getStatus() == FriendStatus.PENDING) {
                friendRequestRepository.delete(friendRequest);
            } else {
                throw new CustomErrorException("Cannot cancel this friend request");
            }
        } else {
            throw new CustomErrorException("Friend request not found");
        }
    }
}