package com.example.snapheal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.entities.User;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
	
	// Lấy danh sách lời mời kết bạn
	@Query("SELECT f.requester FROM FriendRequest f WHERE f.receiver.id = :userId AND f.status = 'PENDING'")
	List<User> findPendingFriendRequests(@Param("userId") Long userId);
	
	Optional<FriendRequest> findByRequesterId(Long requesterId);
}
