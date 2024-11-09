package com.example.snapheal.service;

import com.example.snapheal.dtos.AnnotationDto;
import com.example.snapheal.dtos.UpdateAnnotationImagesDto;
import com.example.snapheal.dtos.UpdateFriendAnnotationDto;
import com.example.snapheal.entities.Annotation;
import com.example.snapheal.entities.AnnotationTag;
import com.example.snapheal.entities.Photo;
import com.example.snapheal.entities.User;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.repository.AnnotationRepository;
import com.example.snapheal.responses.AnnotationDetailResponse;
import com.example.snapheal.responses.AnnotationResponse;
import com.example.snapheal.responses.FriendResponse;
import com.example.snapheal.responses.PhotoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public List<AnnotationResponse> getList() {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Annotation> annotations = annotationRepository.findAnnotationsByOwnerIdAndTaggedUserId(userDetails.getId());
        return annotations.stream().map(Annotation::mapToAnnotationResponse).collect(Collectors.toList());
    }

    public List<AnnotationResponse> search(String query) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Annotation> annotations = annotationRepository.findAnnotationsByOwnerIdAndSearchQuery(userDetails.getId(), query);
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
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
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
                    .createAt(LocalDateTime.now())
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

    public void updateImages(UpdateAnnotationImagesDto dto) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Annotation annotation = annotationRepository.findById(dto.getAnnotationId()).orElseThrow(
                () -> new CustomErrorException("Can not found Annotation with id: " + dto.getAnnotationId())
        );
        List<PhotoResponse> photos = photoService.getPhotosByAnnotationId(dto.getAnnotationId());
        List<String> dtoImgs = dto.getImageUrls();

        List<PhotoResponse> photoRemoveds = photos.stream()
                .filter(obj -> !dtoImgs.contains(obj.getPhotoUrl()))
                .toList();

        for (PhotoResponse photoResponse: photoRemoveds) {
            photoService.delete(photoResponse.getId());
        }

        List<String> oldImgs = photos.stream()
                .map(PhotoResponse::getPhotoUrl)
                .toList();

        List<String> newImgs = dtoImgs.stream()
                .filter(url -> !oldImgs.contains(url))
                .toList();

        for (String img: newImgs) {
            Photo photo = Photo.builder()
                    .annotation(annotation)
                    .photoUrl(img)
                    .createAt(LocalDateTime.now())
                    .createBy(userDetails)
                    .build();
            photoService.save(photo);
        }
    }
}
