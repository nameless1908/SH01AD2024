package com.example.snapheal.repository;

import com.example.snapheal.entities.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnotationTagRepository extends JpaRepository<Photo, Long> {

}
