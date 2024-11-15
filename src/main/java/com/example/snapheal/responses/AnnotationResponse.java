package com.example.snapheal.responses;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnotationResponse implements Serializable {
    private Long id;
    private String title;
    private String name;
    private Double latitude;
    private Double longitude;
    private String address;
    private String thumbnail;
    private FriendResponse owner;
    private Long createAt;
    private Long updateAt;
}
