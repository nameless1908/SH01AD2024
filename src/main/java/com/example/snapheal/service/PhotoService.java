package com.example.snapheal.service;

import com.example.snapheal.entities.Photo;
import com.example.snapheal.repository.PhotoRepository;
import com.example.snapheal.responses.PhotoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotoService {
    @Autowired
    private PhotoRepository photoRepository;

    public void save(Photo photo) {
        photoRepository.save(photo);
    }

    public void delete(Long id) {
        photoRepository.deleteById(id);
    }

    public List<PhotoResponse> getPhotosByAnnotationId(Long annotationId) {
        List<Photo> photos = photoRepository.findByAnnotationId(annotationId);
        return photos.stream().map(Photo::mapToPhotoResponse).toList();
    }
}
