package com.example.snapheal.responses;

import com.example.snapheal.entities.Photo;
import lombok.*;

import java.util.List;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnotationDetailResponse {
    private AnnotationResponse info;
    private List<FriendResponse> friendTagged;
    private List<PhotoResponse> photos;
}
