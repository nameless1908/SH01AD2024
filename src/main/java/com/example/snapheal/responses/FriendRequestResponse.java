package com.example.snapheal.responses;
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
public class FriendRequestResponse {
	private Long id;
	private String username;
	private String fullName;
	private String avatar;
}
