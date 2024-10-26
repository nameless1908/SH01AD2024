package com.example.snapheal.service;

import com.example.snapheal.entities.AnnotationTag;
import com.example.snapheal.entities.User;
import com.example.snapheal.repository.AnnotationTagRepository;
import com.example.snapheal.responses.FriendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnotationTagService {

    @Autowired
    private AnnotationTagRepository annotationTagRepository;

    public List<FriendResponse> getListAnnotationTagByAnnotationId(Long annotationId) {
        List<AnnotationTag> annotationTags = annotationTagRepository.findByAnnotationId(annotationId);

        List<User> users = annotationTags.stream().map(AnnotationTag::getTaggedUser).toList();

        return users.stream().map(User::mapToFriendResponse).toList();
    }

    public void save(AnnotationTag annotationTag) {
        annotationTagRepository.save(annotationTag);
    }

    public void delete(Long id) {
        annotationTagRepository.deleteByTaggedUserId(id);
    }
}
