package com.example.snapheal.repository;

import com.example.snapheal.entities.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    @Query("SELECT DISTINCT a FROM Annotation a " +
            "LEFT JOIN AnnotationTag at ON at.annotation.id = a.id " +
            "WHERE a.owner.id = :userId " +
            "OR at.taggedUser.id = :userId")
    List<Annotation> findAnnotationsByOwnerIdAndTaggedUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT a FROM Annotation a " +
            "LEFT JOIN AnnotationTag at ON at.annotation.id = a.id " +
            "WHERE (a.owner.id = :userId OR at.taggedUser.id = :userId) " +
            "AND (a.title LIKE %:searchQuery% " +
            "OR a.name LIKE %:searchQuery% " +
            "OR a.address LIKE %:searchQuery%)")
    List<Annotation> findAnnotationsByOwnerIdAndSearchQuery(
            @Param("userId") Long userId,
            @Param("searchQuery") String searchQuery);
}
