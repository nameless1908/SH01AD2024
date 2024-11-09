package com.example.snapheal.entities;

import com.example.snapheal.Utils.DateTimeUtils;
import com.example.snapheal.responses.AnnotationResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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

    @Column(name = "latitude", columnDefinition = "double")
    private Double latitude;

    @Column(name = "longitude", columnDefinition = "double")
    private Double longitude;

    private String address;
    private String thumbnail;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "timestamp")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(columnDefinition = "timestamp")
    private LocalDateTime updateAt;

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
                .createAt(createAt != null ? DateTimeUtils.toTimestamp(createAt) : null)
                .updateAt(updateAt != null ? DateTimeUtils.toTimestamp(createAt) : null)
                .build();
    }
}
