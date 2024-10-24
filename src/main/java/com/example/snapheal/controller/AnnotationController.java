package com.example.snapheal.controller;

import com.example.snapheal.entities.Annotation;
import com.example.snapheal.responses.AnnotationResponse;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("${api.prefix}/annotation")
@RestController
public class AnnotationController {
    @Autowired
    private AnnotationService annotationService;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject> getListAnnotation(@RequestParam Long userID) {
        List<AnnotationResponse> annotations = annotationService.getList(userID);

        return  ResponseEntity.ok(
            ResponseObject.builder()
                    .code(200)
                    .message("Successfully!")
                    .data(annotations)
                    .status(HttpStatus.OK)
                    .build()
        );
    }
}
