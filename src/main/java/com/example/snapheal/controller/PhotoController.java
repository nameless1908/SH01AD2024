package com.example.snapheal.controller;

import com.example.snapheal.entities.Photo;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/photo")
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseObject> deletePhotos(@RequestParam("ids") List<Long> ids) {
        if (!ids.isEmpty()) {
            for (Long id : ids) {
                photoService.delete(id);
            }
        }
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .code(HttpStatus.OK.value())
                        .message("Photo deleted successfully")
                        .data(true)
                        .build()
        );
    }
}
