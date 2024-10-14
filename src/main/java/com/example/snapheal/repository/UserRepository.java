package com.example.snapheal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.snapheal.model.User;

public interface UserRepository extends CrudRepository<User, Long>{
	
	@Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm%")
	List<User> findUsersBySearch(@Param("searchTerm") String searchTerm);
}
