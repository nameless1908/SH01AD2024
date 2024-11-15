package com.example.snapheal.responses;

import com.example.snapheal.entities.Photo;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnotationDetailResponse implements Serializable {
    private AnnotationResponse info;
    private List<FriendResponse> friendTagged;
    private List<PhotoResponse> photos;
}
