package com.example.snapheal.entities;

import com.example.snapheal.responses.AnnotationResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@Builder
public class Annotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Title provided by user
    private String name;  // Name of location
    private Double latitude;
    private Double longitude;
    private String address;
    private String thumbnail;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt;

    public AnnotationResponse mapToAnnotationResponse() {
        return AnnotationResponse.builder()
                .id(id)
                .title(title)
                .name(name)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .thumbnail(thumbnail)
                .owner(owner.mapToFriendResponse())
                .createAt(createAt != null ? createAt.getTime() : null)
                .updateAt(updateAt != null ? updateAt.getTime() : null)
                .build();
    }
}
