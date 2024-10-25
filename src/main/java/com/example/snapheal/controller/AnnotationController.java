package com.example.snapheal.controller;

import com.example.snapheal.dtos.AnnotationDto;
import com.example.snapheal.responses.AnnotationDetailResponse;
import com.example.snapheal.responses.AnnotationResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("${api.prefix}/annotation")
@RestController
public class AnnotationController {
    @Autowired
    private AnnotationService annotationService;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject> getListAnnotation(@RequestParam Long userId) {
        List<AnnotationResponse> annotations = annotationService.getList(userId);

        return  ResponseEntity.ok(
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
    public ResponseEntity<ResponseObject> getAnnotationDetail(@RequestParam Long annotationId) {
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
}
