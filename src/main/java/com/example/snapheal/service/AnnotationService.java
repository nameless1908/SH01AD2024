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
import com.example.snapheal.repository.AnnotationTagRepository;
import com.example.snapheal.repository.PhotoRepository;
import com.example.snapheal.responses.AnnotationDetailResponse;
import com.example.snapheal.responses.AnnotationResponse;
import com.example.snapheal.responses.FriendResponse;
import com.example.snapheal.responses.PhotoResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
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

    // Caches the list of annotations for the user.
    @Cacheable(value = "annotations", key = "#userDetails.id")
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

    // Clear the cache when a new annotation is created
    @CacheEvict(value = "annotations", key = "#userDetails.id", allEntries = true)
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

    // Caches annotation details by annotation ID.
    @Cacheable(value = "annotationDetails", key = "#annotationId")
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
                () -> new CustomErrorException("Cannot find Annotation with id: " + dto.getAnnotationId())
        );

        // Lấy danh sách friend hiện tại
        List<FriendResponse> currentFriendTagged = annotationTagService.getListAnnotationTagByAnnotationId(dto.getAnnotationId());
        Set<Long> currentFriendIds = currentFriendTagged.stream()
                .map(FriendResponse::getId)
                .collect(Collectors.toSet());

        // Lấy danh sách friend mới
        Set<Long> newFriendIds = dto.getNewFriends().stream()
                .map(FriendResponse::getId)
                .collect(Collectors.toSet());

        // Xác định friend cần xóa (có trong currentFriend nhưng không có trong newFriend)
        Set<Long> friendsToRemove = new HashSet<>(currentFriendIds);
        friendsToRemove.removeAll(newFriendIds);

        // Xác định friend cần thêm (có trong newFriend nhưng không có trong currentFriend)
        Set<Long> friendsToAdd = new HashSet<>(newFriendIds);
        friendsToAdd.removeAll(currentFriendIds);

        // Xóa các friend không còn tồn tại trong danh sách mới
        for (Long friendId : friendsToRemove) {
            annotationTagService.delete(friendId);
            evictUserCache(friendId);  // Xóa cache cho friend bị xóa
        }

        // Thêm các friend mới vào danh sách
        for (Long friendId : friendsToAdd) {
            User user = userService.findUserById(friendId).orElseThrow(
                    () -> new CustomErrorException("Cannot find User with id: " + friendId)
            );
            AnnotationTag newAnnotationTag = AnnotationTag.builder()
                    .taggedUser(user)
                    .annotation(annotation)
                    .build();
            annotationTagService.save(newAnnotationTag);
            evictUserCache(friendId);  // Xóa cache cho friend mới được thêm
        }
    }

    @CacheEvict(value = "annotations", key = "#userId")
    public void evictUserCache(Long userId) {
        // Phương thức này để xóa cache cho một user cụ thể
    }

    // Clear specific cache entry when updating images
    @CacheEvict(value = "annotationDetails", key = "#dto.annotationId")
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
                    .createAt(new Date())
                    .createBy(userDetails)
                    .build();
            photoService.save(photo);
        }
    }
}
