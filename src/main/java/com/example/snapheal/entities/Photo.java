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

    @ManyToOne
    @JoinColumn(name = "annotation_id")
    private Annotation annotation;

    private String photoUrl;

    @ManyToOne
    @JoinColumn(name = "create_by")
    private User createBy;

    // Thay thế Date bằng LocalDateTime
    @CreationTimestamp
    @Column(name = "create_at", nullable = false, columnDefinition = "timestamp")
    private LocalDateTime createAt;

    // Sử dụng LocalDateTime trong PhotoResponse
    public PhotoResponse mapToPhotoResponse() {
        return PhotoResponse.builder()
                .id(id)
                .photoUrl(photoUrl)
                .createAt(createAt != null ? DateTimeUtils.toTimestamp(createAt) : null) // Chuyển LocalDateTime sang String
                .build();
    }

    // Gán giá trị cho createAt trước khi persist
    @PrePersist
    public void prePersist() {
        if (createAt == null) {
            createAt = LocalDateTime.now();  // Set createAt khi chưa được gán
        }
    }
}
