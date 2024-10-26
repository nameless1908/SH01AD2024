package com.example.snapheal.repository;

import com.example.snapheal.entities.AnnotationTag;
import com.example.snapheal.entities.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationTagRepository extends JpaRepository<AnnotationTag, Long> {
    List<AnnotationTag> findByAnnotationId(Long annotationId);
}
