package com.example.snapheal.repository;

import com.example.snapheal.entities.AnnotationTag;
import com.example.snapheal.entities.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AnnotationTagRepository extends JpaRepository<AnnotationTag, Long> {
    List<AnnotationTag> findByAnnotationId(Long annotationId);
    @Transactional
    void deleteByTaggedUserId(Long taggedUserId);

    @Query("SELECT at FROM AnnotationTag at " +
            "WHERE at.annotation.owner.id = :ownerId " +
            "AND at.taggedUser.id = :taggedId")
    List<AnnotationTag> findAnnotationTagsByOwnerAndTaggedUser(@Param("ownerId") Long ownerId,
                                                               @Param("taggedId") Long taggedId);
}
