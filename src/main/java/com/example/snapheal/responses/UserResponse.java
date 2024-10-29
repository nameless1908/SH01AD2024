package com.example.snapheal.responses;

import lombok.*;

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
	private String status;
}
