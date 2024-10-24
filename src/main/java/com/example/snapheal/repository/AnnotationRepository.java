package com.example.snapheal.repository;

import com.example.snapheal.entities.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

    @Query("SELECT u FROM Annotation u WHERE u.owner.id = :ownerId")
    List<Annotation> findByOwnerId(Long ownerId);
}
