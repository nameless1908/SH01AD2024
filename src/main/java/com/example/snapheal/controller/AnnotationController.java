package com.example.snapheal.controller;

import com.example.snapheal.dtos.AnnotationDto;
import com.example.snapheal.dtos.DeleteAnnotationDto;
import com.example.snapheal.dtos.UpdateAnnotationImagesDto;
import com.example.snapheal.dtos.UpdateFriendAnnotationDto;
import com.example.snapheal.responses.AnnotationDetailResponse;
import com.example.snapheal.responses.AnnotationResponse;
import com.example.snapheal.responses.PhotoResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.AnnotationService;
import com.example.snapheal.service.UploadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("${api.prefix}/annotation")
@RestController
public class AnnotationController {
    @Autowired
    private AnnotationService annotationService;
    @Autowired
    private UploadService uploadService;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject> getListAnnotation() throws JsonProcessingException {
        List<AnnotationResponse> annotations = annotationService.getList();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .code(200)
                        .message("Successfully!")
                        .data(annotations)
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseObject> createAnnotation(@RequestBody AnnotationDto annotationDto) {
        Boolean isSuccess = annotationService.createAnnotation(annotationDto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(isSuccess)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Create Successfully!")
                        .build()
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<ResponseObject> getAnnotationDetail(@RequestParam Long annotationId) throws JsonProcessingException {
        AnnotationDetailResponse response = annotationService.getDetail(annotationId);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(response)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Create Successfully!")
                        .build()
        );
    }

    @PutMapping("/update-friend")
    public ResponseEntity<ResponseObject> updateFriends(@RequestBody UpdateFriendAnnotationDto dto) {
        annotationService.updateFriendTagged(dto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(true)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Create Successfully!")
                        .build()
        );
    }

    @PutMapping("/update-images/{annotationId}")
    public ResponseEntity<ResponseObject> updateImages(@PathVariable("annotationId") Long annotationId,
                                                       @RequestBody MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadService.uploadImage(file);
            urls.add(url);
        }
        UpdateAnnotationImagesDto dto = new UpdateAnnotationImagesDto(
                annotationId,
                urls
        );
        List<PhotoResponse> list = annotationService.updateImages(dto);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .data(list)
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Create Successfully!")
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseObject> searchAnnotation(@RequestParam String query) {
        List<AnnotationResponse> annotations = annotationService.search(query);

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .code(200)
                        .message("Successfully!")
                        .data(annotations)
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseObject> deleteAnnotation(@RequestBody DeleteAnnotationDto deleteAnnotationDto) {
        annotationService.deleteAnnotation(deleteAnnotationDto.getAnnotationId());

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .code(200)
                        .message("Delete Successfully!")
                        .data(true)
                        .status(HttpStatus.OK)
                        .build()
        );
    }
}
