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
public class FriendResponse {
	private String username;
	private String fullname;
	private String avatar;
	
	public static FriendResponse fromUser(User user) {
        FriendResponse friendResponse = new FriendResponse();
        friendResponse.setUsername(user.getUsername());
        friendResponse.setFullname(user.getFullName());
        friendResponse.setAvatar(user.getAvatar());
        return friendResponse;
    }
}
