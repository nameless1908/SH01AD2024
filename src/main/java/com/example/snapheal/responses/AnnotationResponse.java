package com.example.snapheal.responses;

import lombok.*;

import java.sql.Timestamp;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AnnotationResponse {
    private Long id;
    private String title;
    private String name;
    private Double latitude;
    private Double longitude;
    private String address;
    private String thumbnail;
    private FriendResponse owner;
    private Timestamp createAt;
    private Timestamp updateAt;
}
