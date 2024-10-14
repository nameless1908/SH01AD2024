package com.example.snapheal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.snapheal.model.User;
import com.example.snapheal.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	public List<User> searchUser(String searchTerm){
		return userRepository.findUsersBySearch(searchTerm);
	}
}
