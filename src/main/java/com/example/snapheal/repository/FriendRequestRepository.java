package com.example.snapheal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.snapheal.model.FriendRequest;
import com.example.snapheal.model.User;

public interface FriendRequestRepository extends CrudRepository<FriendRequest, Long> {
	
	// Lấy danh sách lời mời kết bạn
	@Query("SELECT f.requester FROM FriendRequest f WHERE f.receiver.id = :userId AND f.status = 'PENDING'")
	List<User> findPendingFriendRequests(@Param("userId") Long userId);
}
