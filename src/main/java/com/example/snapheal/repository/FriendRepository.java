package com.example.snapheal.repository;

import com.example.snapheal.entities.Friend;
import com.example.snapheal.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // Lấy danh sách bạn bè của một người dùng dựa trên ID của người dùng đó
//    @Query("SELECT f.friend FROM Friend f WHERE f.user.id = :userId OR f.friend.id = :userId" )
//    List<User> findFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.friend FROM Friend f WHERE f.user.id = :userId")
    List<User> findFriendsWhereUserIsUser(@Param("userId") Long userId);

    @Query("SELECT f.user FROM Friend f WHERE f.friend.id = :userId")
    List<User> findFriendsWhereUserIsFriend(@Param("userId") Long userId);

    //Tìm kiếm bạn bè
    @Query("SELECT f.friend FROM Friend f WHERE f.user.id = :userId AND (f.friend.username LIKE %:searchTerm% OR f.friend.fullName LIKE %:searchTerm%)")
    List<User> findFriendsBySearch(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);
    
    void deleteByUserIdAndFriendId(Long userId, Long friendId);
    
}

