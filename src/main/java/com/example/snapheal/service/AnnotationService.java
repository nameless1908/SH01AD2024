package com.example.snapheal.service;

import com.example.snapheal.dtos.AnnotationDto;
import com.example.snapheal.dtos.UpdateFriendAnnotationDto;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnotationService {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnotationTagService annotationTagService;

    @Autowired
    private PhotoService photoService;
    @PersistenceContext
    private EntityManager entityManager;
    public List<AnnotationResponse> getList() {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Annotation> annotations = annotationRepository.findAnnotationsByOwnerIdAndTaggedUserId(userDetails.getId());
        return annotations.stream().map(Annotation::mapToAnnotationResponse).collect(Collectors.toList());
    }
    @Transactional
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
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Annotation annotation = annotationRepository.findById(annotationId).orElseThrow(
                () -> new CustomErrorException("Can not found Annotation with id: " + annotationId)
        );

        List<PhotoResponse> photoResponses = photoService.getPhotosByAnnotationId(annotationId);

        AnnotationDetailResponse response = AnnotationDetailResponse.builder()
                .info(annotation.mapToAnnotationResponse())
                .photos(photoResponses)
                .build();
        if (Objects.equals(annotation.getOwner().getId(), userDetails.getId())) {
            List<FriendResponse> friendTagged = annotationTagService.getListAnnotationTagByAnnotationId(annotationId);
            response.setFriendTagged(friendTagged);
        } else {
            response.setFriendTagged(new ArrayList<>());
        }

        return response;
    }

    public void updateFriendTagged(UpdateFriendAnnotationDto dto) {
        Annotation annotation = annotationRepository.findById(dto.getAnnotationId()).orElseThrow(
                () -> new CustomErrorException("Can not found Annotation with id: " + dto.getAnnotationId())
        );
        List<FriendResponse> friendTagged = annotationTagService.getListAnnotationTagByAnnotationId(dto.getAnnotationId());
        for (FriendResponse friendResponse: friendTagged) {
            annotationTagService.delete(friendResponse.getId());
        }

        for (FriendResponse friendResponse: dto.getNewFriends()) {
            User user = userService.findUserById(friendResponse.getId()).orElseThrow(
                    () -> new CustomErrorException("Can not found User with id: " + friendResponse.getId())
            );
            AnnotationTag newAnnotationTag = AnnotationTag.builder()
                    .taggedUser(user)
                    .annotation(annotation)
                    .build();
            annotationTagService.save(newAnnotationTag);
        }
    }
}
