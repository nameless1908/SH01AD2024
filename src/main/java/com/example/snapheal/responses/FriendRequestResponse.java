package com.example.snapheal.responses;
import com.example.snapheal.entities.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FriendRequestResponse {
	private String username;
	private String fullname;
	private String avatar;
		
	public static FriendRequestResponse fromUser(User user) {
		FriendRequestResponse friendRequestResponse = new FriendRequestResponse();
		friendRequestResponse.setUsername(user.getUsername());
		friendRequestResponse.setFullname(user.getFullName());
		friendRequestResponse.setAvatar(user.getAvatar());
		return friendRequestResponse;
	}
}
