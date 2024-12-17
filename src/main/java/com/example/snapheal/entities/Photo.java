package com.example.snapheal.entities;

import com.example.snapheal.Utils.DateTimeUtils;
import com.example.snapheal.responses.PhotoResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String photoUrl;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "annotation_id")
    private Annotation annotation;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    // Thay thế Date bằng LocalDateTime
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp")
    private LocalDateTime createdAt;

    // Sử dụng LocalDateTime trong PhotoResponse
    public PhotoResponse mapToPhotoResponse() {
        return PhotoResponse.builder()
                .id(id)
                .photoUrl(photoUrl)
                .createAt(createdAt != null ? DateTimeUtils.toTimestamp(createdAt) : null) // Chuyển LocalDateTime sang String
                .build();
    }

    // Gán giá trị cho createAt trước khi persist
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();  // Set createAt khi chưa được gán
        }
    }
}
