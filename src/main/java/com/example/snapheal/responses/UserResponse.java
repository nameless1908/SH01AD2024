package com.example.snapheal.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponse {
	private Long id;
	private String username;
	private String fullname;
	private String avatar;
	private String status;
	
	public static UserResponse fromUser(User user, String status) {
		UserResponse userResponse = new UserResponse();
		userResponse.setUsername(user.getUsername());
		userResponse.setFullname(user.getFullName());
		userResponse.setAvatar(user.getAvatar());
		userResponse.setStatus(status);
        return userResponse;
	}
}
