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
public class UpdateFriendAnnotationDto {
    private Long annotationId;
    private List<FriendResponse> newFriends;
}
