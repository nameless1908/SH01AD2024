package com.example.snapheal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AnnotationDto {
    private String title;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private List<Long> taggedIds;
    private List<String> images;
}
