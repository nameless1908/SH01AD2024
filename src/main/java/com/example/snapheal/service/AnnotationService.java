package com.example.snapheal.service;

import com.example.snapheal.entities.Annotation;
import com.example.snapheal.repository.AnnotationRepository;
import com.example.snapheal.responses.AnnotationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnotationService {

    @Autowired
    private AnnotationRepository annotationRepository;

    public List<AnnotationResponse> getList(Long ownerId) {
        List<Annotation> annotations = annotationRepository.findByOwnerId(ownerId);
        return annotations.stream().map(Annotation::mapToAnnotationResponse).collect(Collectors.toList());
    }
}
