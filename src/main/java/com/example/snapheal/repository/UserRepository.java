package com.example.snapheal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.snapheal.entities.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	@Query(value = "SELECT u.*, " +
	        "CASE " +
	        "   WHEN fr.requester_id IS NOT NULL THEN fr.status " +
	        "   ELSE 'NONE' " +
	        "END AS friend_status " +
	        "FROM USER u " +
	        "LEFT JOIN Friend_Request fr " +
	        "ON (u.id = fr.requester_id AND fr.receiver_id = :currentUserId) " +
	        "OR (u.id = fr.receiver_id AND fr.requester_id = :currentUserId) " +
	        "WHERE (u.username LIKE CONCAT('%', :searchTerm, '%') " +
	        "OR u.full_name LIKE CONCAT('%', :searchTerm, '%')) " +
	        "AND u.id != :currentUserId", 
	        nativeQuery = true)
	List<Object[]> searchUsersWithFriendStatus(@Param("currentUserId") Long currentUserId, @Param("searchTerm") String searchTerm);

	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	@Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
	Optional<User> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);
	
	@Query("SELECT u FROM User u WHERE u.id != :currentUserId AND " +
		       "u.id NOT IN (SELECT f.friend.id FROM Friend f WHERE f.user.id = :currentUserId)")
		List<User> findUsersExcludingFriends(@Param("currentUserId") Long currentUserId);

}
