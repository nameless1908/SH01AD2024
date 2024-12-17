package com.example.snapheal.entities;

import com.example.snapheal.Utils.DateTimeUtils;
import com.example.snapheal.responses.AnnotationResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "annotation", cascade = CascadeType.ALL)
    List<Photo> photos;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "timestamp", name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(columnDefinition = "timestamp", name = "updated_at")
    private LocalDateTime updatedAt;

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
                .createAt(createdAt != null ? DateTimeUtils.toTimestamp(createdAt) : null)
                .updateAt(updatedAt != null ? DateTimeUtils.toTimestamp(createdAt) : null)
                .build();
    }
}
