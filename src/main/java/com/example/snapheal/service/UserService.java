package com.example.snapheal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.model.User;
import com.example.snapheal.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
	
	public List<Object[]> searchUserWithFriendRequestStatus(Long currentUserId, String searchTerm) {
	    return userRepository.searchUsersWithFriendStatus(currentUserId, searchTerm);
	}
}
