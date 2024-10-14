package com.example.snapheal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.model.Friend;
import com.example.snapheal.model.User;
import com.example.snapheal.repository.FriendRepository;

import jakarta.transaction.Transactional;

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
    public List<User> getAllFriends(Long userId) {
        return friendRepository.findFriendsByUserId(userId);
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

