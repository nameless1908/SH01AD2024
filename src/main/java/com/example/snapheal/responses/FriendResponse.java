package com.example.snapheal.responses;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatar;
}
