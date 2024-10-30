package com.example.snapheal.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCurrentLocationDto {
	private Long id;
	private Double currentLatitude;
	private Double currentLongitude;
}
