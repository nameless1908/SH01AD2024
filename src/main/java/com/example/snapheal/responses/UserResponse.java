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
public class UserResponse {
	private String username;
	private String fullname;
	private String avatar;
	private String status;
}
