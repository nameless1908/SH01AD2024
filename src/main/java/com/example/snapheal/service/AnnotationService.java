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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cglib.core.Local;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String annotationsCache(Long id) {
        return "annotations:" + id;
    }

    private String annotationDetailsCacheKey(Long id) {
        return "annotationDetail:" + id;
    }

    // Caches the list of annotations for the user.
    public List<AnnotationResponse> getList() throws JsonProcessingException {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cacheKey = annotationsCache(userDetails.getId());

        // Kiểm tra xem cache có dữ liệu không
        if (redisTemplate.hasKey(cacheKey)) {
            // Trả về dữ liệu từ cache, deserialize lại thành List<AnnotationResponse>
            String json = (String) redisTemplate.opsForValue().get(cacheKey);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<List<AnnotationResponse>>() {
            });
        } else {
            // Nếu không có, lấy dữ liệu từ DB và cache lại
            List<Annotation> annotations = annotationRepository.findAnnotationsByOwnerIdAndTaggedUserId(userDetails.getId());
            List<AnnotationResponse> response = annotations.stream()
                    .map(Annotation::mapToAnnotationResponse)
                    .collect(Collectors.toList());

            // Lưu dữ liệu vào Redis dưới dạng JSON
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(response);
                redisTemplate.opsForValue().set(cacheKey, json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return response;
        }
    }

    public List<AnnotationResponse> search(String query) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Annotation> annotations = annotationRepository.findAnnotationsByOwnerIdAndSearchQuery(userDetails.getId(), query);
        return annotations.stream().map(Annotation::mapToAnnotationResponse).collect(Collectors.toList());
    }

    // Clear the cache when a new annotation is created
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
                .createAt(LocalDateTime.now())
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
                        .createAt(LocalDateTime.now())
                        .build();
                annotationTagService.save(newAnnotationTag);
                String cacheKey = annotationsCache(userTagged.get().getId());
                redisTemplate.delete(cacheKey);
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

        redisTemplate.delete(annotationsCache(userDetails.getId()));
        return true;
    }

    // Caches annotation details by annotation ID.
    public AnnotationDetailResponse getDetail(Long annotationId) throws JsonProcessingException {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cacheKey = annotationDetailsCacheKey(annotationId);

        // Check if the cache exists
        if (redisTemplate.hasKey(cacheKey)) {
            // Retrieve cached data as a JSON string and deserialize it into AnnotationDetailResponse
            String cachedJson = (String) redisTemplate.opsForValue().get(cacheKey);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(cachedJson, AnnotationDetailResponse.class);
        } else {
            // Fetch data from the database
            Annotation annotation = annotationRepository.findById(annotationId).orElseThrow(
                    () -> new CustomErrorException("Cannot find Annotation with id: " + annotationId)
            );

            List<PhotoResponse> photoResponses = photoService.getPhotosByAnnotationId(annotationId);

            // Prepare the response object
            AnnotationDetailResponse response = AnnotationDetailResponse.builder()
                    .info(annotation.mapToAnnotationResponse())
                    .photos(photoResponses)
                    .build();

            // Set friend tagged data for the owner
            if (Objects.equals(annotation.getOwner().getId(), userDetails.getId())) {
                List<FriendResponse> friendTagged = annotationTagService.getListAnnotationTagByAnnotationId(annotationId);
                response.setFriendTagged(friendTagged);
            } else {
                response.setFriendTagged(new ArrayList<>());
            }

            // Serialize response object to JSON and cache it
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, jsonResponse);
            return response;
        }
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
        }

        // Thêm các friend mới vào danh sách
        for (Long friendId : friendsToAdd) {
            User user = userService.findUserById(friendId).orElseThrow(
                    () -> new CustomErrorException("Cannot find User with id: " + friendId)
            );
            AnnotationTag newAnnotationTag = AnnotationTag.builder()
                    .taggedUser(user)
                    .annotation(annotation)
                    .createAt(LocalDateTime.now())
                    .build();
            annotationTagService.save(newAnnotationTag);
        }
        redisTemplate.delete(annotationDetailsCacheKey(dto.getAnnotationId()));  // Xóa cache cho annotation detail
    }

    // Clear specific cache entry when updating images
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
        redisTemplate.delete(annotationDetailsCacheKey(dto.getAnnotationId()));
    }
}
