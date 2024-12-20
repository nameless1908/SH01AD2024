package com.example.snapheal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.snapheal.entities.FriendRequest;
import com.example.snapheal.enums.FriendStatus;
import com.example.snapheal.entities.User;

import jakarta.transaction.Transactional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
	
	// Lấy danh sách lời mời kết bạn
	@Query("SELECT f.requester FROM FriendRequest f WHERE f.receiver.id = :userId AND f.status = 'PENDING'")
	List<User> findPendingFriendRequests(@Param("userId") Long userId);
	
	Optional<FriendRequest> findByRequesterId(Long requesterId);

	@Query("SELECT fr FROM FriendRequest fr " +
			"WHERE (fr.requester.id = :targetUserId AND fr.receiver.id = :userId)")
	Optional<FriendRequest> findRequestByRequesterId(@Param("userId") Long userId,
													 @Param("targetUserId") Long targetUserId);

	@Query("SELECT fr FROM FriendRequest fr " +
			"WHERE (fr.requester.id = :userId AND fr.receiver.id = :targetUserId) " +
			"   OR (fr.requester.id = :targetUserId AND fr.receiver.id = :userId)")
	Optional<FriendRequest> findFriendRequestByUserIds(@Param("userId") Long userId,
													   @Param("targetUserId") Long targetUserId);

	@Query("SELECT fr FROM FriendRequest fr WHERE fr.requester.id = :requesterId AND fr.receiver.id = :receiverId AND fr.status = :status")
	Optional<FriendRequest> findByRequesterAndReceiverAndStatus(@Param("requesterId") Long requesterId, @Param("receiverId") Long receiverId, @Param("status") FriendStatus status);

	@Query("SELECT fr FROM FriendRequest fr WHERE fr.requester.id = :requesterId AND fr.receiver.id = :receiverId AND fr.status = 'PENDING'")
    Optional<FriendRequest> findPendingRequestByUserIds(@Param("requesterId") Long requesterId, @Param("receiverId") Long receiverId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM FriendRequest fr WHERE " +
	       "((fr.requester.id = :userId AND fr.receiver.id = :friendId) " +
	       "OR (fr.requester.id = :friendId AND fr.receiver.id = :userId)) " +
	       "AND fr.status = 'ACCEPTED'")
	void deleteAcceptedRequest(@Param("userId") Long userId, @Param("friendId") Long friendId);

}
