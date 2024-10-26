package com.example.snapheal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FriendRequestDto {
	private String username;
	private String fullname;
	private String avatar;
	private String friendStatus;
}
