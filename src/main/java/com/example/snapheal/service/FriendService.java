package com.example.snapheal.service;

import com.example.snapheal.responses.FriendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.snapheal.entities.FriendStatus;
import com.example.snapheal.entities.User;
import com.example.snapheal.repository.FriendRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;


    // Tìm kiếm bạn bè
    public List<FriendResponse> searchFriends(Long userId, String searchTerm) {
    	List<User> friends = new ArrayList<>();
    	friends.addAll(friendRepository.findFriendsBySearch(userId, searchTerm));
    	return friends.stream()
                .map(user -> user.mapToFriendResponse(FriendStatus.ACCEPTED))
                .toList();
    }
    
    // Lấy toàn bộ danh sách bạn bè của người dùng theo ID
    public List<FriendResponse> getAllFriends(Long userId) {
        List<User> friends = new ArrayList<>();
        friends.addAll(friendRepository.findFriendsWhereUserIsUser(userId));
        friends.addAll(friendRepository.findFriendsWhereUserIsFriend(userId));
        return friends.stream()
                .map(user -> user.mapToFriendResponse(FriendStatus.ACCEPTED))
                .toList();
    }
    
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
    	friendRepository.deleteByUserIdAndFriendId(userId, friendId);
    }
}

