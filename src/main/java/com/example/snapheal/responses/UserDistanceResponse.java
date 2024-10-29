package com.example.snapheal.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDistanceResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatar;
    private double distance;
}
