package com.example.snapheal.responses;


import com.example.snapheal.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse {
	private Long id;
	private String username;
	private String fullName;
	private String avatar;
	private Status status;
}
