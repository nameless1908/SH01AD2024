package com.example.snapheal.service;

import com.example.snapheal.dtos.AnnotationDto;
import com.example.snapheal.entities.Annotation;
import com.example.snapheal.entities.AnnotationTag;
import com.example.snapheal.entities.Photo;
import com.example.snapheal.entities.User;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.repository.AnnotationRepository;
import com.example.snapheal.repository.AnnotationTagRepository;
import com.example.snapheal.repository.PhotoRepository;
import com.example.snapheal.responses.AnnotationDetailResponse;
import com.example.snapheal.responses.AnnotationResponse;
import com.example.snapheal.responses.FriendResponse;
import com.example.snapheal.responses.PhotoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnnotationService {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnotationTagService annotationTagService;

    @Autowired
    private PhotoService photoService;

    public List<AnnotationResponse> getList(Long userId) {
        List<Annotation> annotations = annotationRepository.findAnnotationsByOwnerIdAndTaggedUserId(userId);
        return annotations.stream().map(Annotation::mapToAnnotationResponse).collect(Collectors.toList());
    }

    public boolean createAnnotation(AnnotationDto annotationDto) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Annotation newAnnotation = Annotation.builder()
                .title(annotationDto.getTitle())
                .name(annotationDto.getName())
                .address(annotationDto.getAddress())
                .latitude(annotationDto.getLatitude())
                .longitude(annotationDto.getLongitude())
                .createAt(new Date())
                .updateAt(new Date())
                .owner(userDetails)
                .build();
        annotationRepository.save(newAnnotation);
        if (!annotationDto.getImages().isEmpty()) {
            newAnnotation.setThumbnail(annotationDto.getImages().get(0));
        }

        for (Long taggedId: annotationDto.getTaggedIds()) {
            Optional<User> userTagged = userService.findUserById(taggedId);
            if (userTagged.isPresent() && !Objects.equals(taggedId, userDetails.getId())) {
                AnnotationTag newAnnotationTag = AnnotationTag.builder()
                        .annotation(newAnnotation)
                        .taggedUser(userTagged.get())
                        .build();
                annotationTagService.save(newAnnotationTag);
            }
        }

        for (String image: annotationDto.getImages()) {
            Photo photo = Photo.builder()
                    .annotation(newAnnotation)
                    .createBy(userDetails)
                    .photoUrl(image)
                    .createAt(new Date())
                    .build();
            photoService.save(photo);
        }
        return true;
    }

    public AnnotationDetailResponse getDetail(Long annotationId) {
        Annotation annotation = annotationRepository.findById(annotationId).orElseThrow(
                () -> new CustomErrorException("Can not found Annotation with id: " + annotationId)
        );

        List<FriendResponse> friendTagged = annotationTagService.getListAnnotationTagByAnnotationId(annotationId);

        List<PhotoResponse> photoResponses = photoService.getPhotosByAnnotationId(annotationId);

        return AnnotationDetailResponse.builder()
                .info(annotation.mapToAnnotationResponse())
                .friendTagged(friendTagged)
                .photos(photoResponses)
                .build();
    }
}
