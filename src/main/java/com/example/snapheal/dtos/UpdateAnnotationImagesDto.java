package com.example.snapheal.dtos;

import com.example.snapheal.responses.FriendResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateAnnotationImagesDto {
    private Long annotationId;
    private List<String> imageUrls;
}