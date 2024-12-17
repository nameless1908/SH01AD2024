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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String annotationsCache(Long id) {
        return "annotations:" + id;
    }

    private String annotationDetailsCacheKey(Long id) {
        return "annotationDetail:" + id;
    }

    // Caches the list of annotations for the user.
    public List<AnnotationResponse> getList() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = auth.getPrincipal();
        User userDetails = (User) principal;
        String cacheKey = annotationsCache(userDetails.getId());

        try {
            // Kiểm tra xem cache có dữ liệu không
            if (redisTemplate.hasKey(cacheKey)) {
                // Trả về dữ liệu từ cache, deserialize lại thành List<AnnotationResponse>
                String json = (String) redisTemplate.opsForValue().get(cacheKey);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(json, new TypeReference<List<AnnotationResponse>>() {});
            }
        } catch (Exception e) {
            // Log lỗi Redis và tiếp tục truy vấn từ MySQL
            System.err.println("Redis không khả dụng: " + e.getMessage());
        }

        // Nếu không có dữ liệu trong Redis hoặc Redis lỗi, lấy từ MySQL
        List<Annotation> annotations = annotationRepository.findAnnotationsByOwnerIdAndTaggedUserId(userDetails.getId());
        List<AnnotationResponse> response = annotations.stream()
                .map(Annotation::mapToAnnotationResponse)
                .collect(Collectors.toList());

        // Lưu dữ liệu vào Redis dưới dạng JSON (nếu Redis khả dụng)
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json);
        } catch (Exception e) {
            // Log lỗi nếu không lưu được vào Redis
            System.err.println("Không thể lưu dữ liệu vào Redis: " + e.getMessage());
        }

        return response;
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

        // Tạo Annotation mới
        Annotation newAnnotation = Annotation.builder()
                .title(annotationDto.getTitle())
                .name(annotationDto.getName())
                .address(annotationDto.getAddress())
                .latitude(annotationDto.getLatitude())
                .longitude(annotationDto.getLongitude())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .owner(userDetails)
                .build();
        annotationRepository.save(newAnnotation);

        if (!annotationDto.getImages().isEmpty()) {
            newAnnotation.setThumbnail(annotationDto.getImages().get(0));
        }

        // Tạo AnnotationTag và xóa cache của tagged users
        for (Long taggedId : annotationDto.getTaggedIds()) {
            Optional<User> userTagged = userService.findUserById(taggedId);
            if (userTagged.isPresent() && !Objects.equals(taggedId, userDetails.getId())) {
                AnnotationTag newAnnotationTag = AnnotationTag.builder()
                        .annotation(newAnnotation)
                        .taggedUser(userTagged.get())
                        .createdAt(LocalDateTime.now())
                        .build();
                annotationTagService.save(newAnnotationTag);

                // Xóa cache của tagged user
                String cacheKey = annotationsCache(userTagged.get().getId());
                try {
                    redisTemplate.delete(cacheKey);
                } catch (Exception e) {
                    System.err.println("Redis error while deleting cache for key: " + cacheKey);
                    e.printStackTrace();
                }
            }
        }

        // Lưu ảnh liên kết với Annotation
        for (String image : annotationDto.getImages()) {
            Photo photo = Photo.builder()
                    .annotation(newAnnotation)
                    .createdBy(userDetails)
                    .photoUrl(image)
                    .createdAt(LocalDateTime.now())
                    .build();
            photoService.save(photo);
        }

        // Xóa cache của user hiện tại
        String userCacheKey = annotationsCache(userDetails.getId());
        try {
            redisTemplate.delete(userCacheKey);
        } catch (Exception e) {
            System.err.println("Redis error while deleting cache for key: " + userCacheKey);
            e.printStackTrace();
        }

        return true;
    }

    // Caches annotation details by annotation ID.
    public AnnotationDetailResponse getDetail(Long annotationId) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cacheKey = annotationDetailsCacheKey(annotationId);

        try {
            // Kiểm tra nếu dữ liệu tồn tại trong Redis
            if (redisTemplate.hasKey(cacheKey)) {
                // Lấy dữ liệu từ Redis và deserialize
                String cachedJson = (String) redisTemplate.opsForValue().get(cacheKey);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(cachedJson, AnnotationDetailResponse.class);
            }
        } catch (Exception e) {
            // Log lỗi và tiếp tục xử lý bằng MySQL
            System.err.println("Redis không khả dụng: " + e.getMessage());
        }

        // Truy vấn từ MySQL nếu không có trong Redis hoặc Redis lỗi
        Annotation annotation = annotationRepository.findById(annotationId).orElseThrow(
                () -> new CustomErrorException("Cannot find Annotation with id: " + annotationId)
        );

        List<PhotoResponse> photoResponses = photoService.getPhotosByAnnotationId(annotationId);

        // Chuẩn bị đối tượng response
        AnnotationDetailResponse response = AnnotationDetailResponse.builder()
                .info(annotation.mapToAnnotationResponse())
                .photos(photoResponses)
                .build();

        // Thêm thông tin bạn bè được gắn thẻ (nếu là chủ sở hữu)
        if (Objects.equals(annotation.getOwner().getId(), userDetails.getId())) {
            List<FriendResponse> friendTagged = annotationTagService.getListAnnotationTagByAnnotationId(annotationId);
            response.setFriendTagged(friendTagged);
        } else {
            response.setFriendTagged(new ArrayList<>());
        }

        // Lưu vào Redis nếu Redis khả dụng
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, jsonResponse);
        } catch (Exception e) {
            // Log lỗi nếu không lưu được vào Redis
            System.err.println("Không thể lưu dữ liệu vào Redis: " + e.getMessage());
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
        }

        // Thêm các friend mới vào danh sách
        for (Long friendId : friendsToAdd) {
            User user = userService.findUserById(friendId).orElseThrow(
                    () -> new CustomErrorException("Cannot find User with id: " + friendId)
            );
            AnnotationTag newAnnotationTag = AnnotationTag.builder()
                    .taggedUser(user)
                    .annotation(annotation)
                    .createdAt(LocalDateTime.now())
                    .build();
            annotationTagService.save(newAnnotationTag);
        }
        try {
            redisTemplate.delete(annotationDetailsCacheKey(dto.getAnnotationId()));  // Xóa cache cho annotation detail
        } catch (Exception e) {
            System.err.println("Không thể xoá liệu  Redis: " + e.getMessage());
        }
    }

    // Clear specific cache entry when updating images
    public List<PhotoResponse> updateImages(UpdateAnnotationImagesDto dto) {
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
                    .createdAt(LocalDateTime.now())
                    .createdBy(userDetails)
                    .build();
            photoService.save(photo);
        }
        try {
            redisTemplate.delete(annotationDetailsCacheKey(dto.getAnnotationId()));
        } catch (Exception e) {
            System.err.println("Không thể xoá liệu  Redis: " + e.getMessage());
        }
        return photoService.getPhotosByAnnotationId(dto.getAnnotationId());
    }

    @Transactional
    public void deleteAnnotation(Long annotationId) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Annotation annotation = annotationRepository.findById(annotationId).orElseThrow(
                () -> new CustomErrorException("Not found Annotation by annotationID!")
        );
        if (!Objects.equals(userDetails.getId(), annotation.getOwner().getId())) {
            throw  new CustomErrorException("Can't remove annotation because you not is owner!");
        }

        try {
            redisTemplate.delete(annotationsCache(annotationId));
            redisTemplate.delete(annotationsCache(userDetails.getId()));
        } catch (Exception e) {
            System.err.println("Không thể xoá liệu  Redis: " + e.getMessage());
        }


        List<FriendResponse> annotationTags = annotationTagService.getListAnnotationTagByAnnotationId(annotationId);
        for(FriendResponse friend: annotationTags) {
            try {
                redisTemplate.delete(annotationsCache(friend.getId()));
            } catch (Exception e) {
                System.err.println("Không thể xoá liệu  Redis: " + e.getMessage());
            }
            annotationTagService.delete(friend.getId());
        }
        List<PhotoResponse> photoResponses = photoService.getPhotosByAnnotationId(annotationId);
        for (PhotoResponse photoResponse: photoResponses) {
            photoService.delete(photoResponse.getId());
        }
        annotationRepository.deleteById(annotationId);
    }
}
