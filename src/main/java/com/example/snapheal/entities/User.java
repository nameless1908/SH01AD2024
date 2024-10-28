package com.example.snapheal.entities;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.example.snapheal.responses.FriendRequestResponse;
import com.example.snapheal.responses.FriendResponse;
import com.example.snapheal.responses.ProfileResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

	@Column(unique = true, nullable = false)
	private String username;

	private String fullName;
	@Column(nullable = false)
    private String password;
    private String avatar;
    private Double currentLatitude;
    private Double currentLongitude;
    
	@CreationTimestamp
	@Column(updatable = false, name = "created_at")
	private Date createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Date updatedAt;

	@Override
	public boolean isAccountNonExpired() {
		return UserDetails.super.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return UserDetails.super.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return UserDetails.super.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return UserDetails.super.isEnabled();
	}

    @Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getPassword() {
		return password;
	}


	public FriendResponse mapToFriendResponse(FriendStatus status) {
		return FriendResponse.builder()
				.id(id)
				.avatar(avatar)
				.username(username)
				.fullName(fullName)
				.status(status)
				.build();
	}
	
	public FriendRequestResponse mapToFriendRequestResponse(FriendStatus status) {
		return FriendRequestResponse.builder()
				.id(id)
				.avatar(avatar)
				.username(username)
				.fullName(fullName)
				.status(status)
				.build();
	}
	
	public ProfileResponse mapToProfileResponse() {
		return ProfileResponse.builder()
				.id(id)
				.avatar(avatar)
				.username(username)
				.fullName(fullName)
				.email(email)
				.currentLongitude(currentLongitude)
				.currentLatitude(currentLatitude)
				.build();
	}

	public FriendResponse mapToFriendResponse() {
		return FriendResponse.builder()
				.id(id)
				.avatar(avatar)
				.username(username)
				.fullName(fullName)
				.build();
	}
}
