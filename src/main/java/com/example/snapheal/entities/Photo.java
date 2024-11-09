package com.example.snapheal.entities;

import com.example.snapheal.responses.PhotoResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

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

    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    public PhotoResponse mapToPhotoResponse() {
        return PhotoResponse.builder()
                .id(id)
                .photoUrl(photoUrl)
                .createAt(createAt != null ? createAt.getTime() : null)
                .build();
    }
}
