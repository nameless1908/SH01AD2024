package com.example.snapheal.dtos;
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
public class UpdateUserDto {
	private Long id;
	private String username;
	private String fullname;
	private String email;
	private String avatar;
}

