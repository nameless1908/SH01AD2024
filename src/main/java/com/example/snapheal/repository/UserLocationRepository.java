package com.example.snapheal.repository;

import com.example.snapheal.entities.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    Optional<UserLocation> findByUserId(Long userId);
}
