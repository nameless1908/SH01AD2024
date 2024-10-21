package com.example.snapheal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.snapheal.entities.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long>{
	
	@Query(value = "SELECT u.*, " +
	        "CASE " +
	        "   WHEN fr.requester_id IS NOT NULL THEN fr.status " +
	        "   ELSE NULL " +
	        "END AS friend_status " +
	        "FROM USER u " +
	        "LEFT JOIN Friend_Request fr " +
	        "ON (u.id = fr.requester_id AND fr.receiver_id = :currentUserId) " +
	        "OR (u.id = fr.receiver_id AND fr.requester_id = :currentUserId) " +
	        "WHERE u.username LIKE CONCAT('%', :searchTerm, '%')", 
	        nativeQuery = true)
	List<Object[]> searchUsersWithFriendStatus(@Param("currentUserId") Long currentUserId, @Param("searchTerm") String searchTerm);

	Optional<User> findByEmail(String email);
//	Optional<User> findByUsername(String username);
}
