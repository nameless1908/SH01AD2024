package com.example.snapheal.service;

import com.example.snapheal.responses.FriendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.entities.Friend;
import com.example.snapheal.entities.User;
import com.example.snapheal.repository.FriendRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;


    // Tìm kiếm bạn bè
    public List<User> searchFriends(Long userId, String searchTerm) {
        return friendRepository.findFriendsBySearch(userId, searchTerm);
    }
    
    // Lấy toàn bộ danh sách bạn bè của người dùng theo ID
    public List<FriendResponse> getAllFriends(Long userId) {
        List<User> friends = new ArrayList<>();
        friends.addAll(friendRepository.findFriendsWhereUserIsUser(userId));
        friends.addAll(friendRepository.findFriendsWhereUserIsFriend(userId));
        return friends.stream().map(User::mapToFriendResponse).toList();
    }
    
 // Lấy tất cả bạn bè
    public List<Friend> getAllFriends() {
        return friendRepository.findAll();
    }

    // Lấy bạn bè theo ID
    public Optional<Friend> getFriendById(Long id) {
        return friendRepository.findById(id);
    }
    
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
    	friendRepository.deleteByUserIdAndFriendId(userId, friendId);
    }
}

