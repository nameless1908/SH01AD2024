package com.example.snapheal.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class
AnnotationTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "annotation_id")
    private Annotation annotation;

    @ManyToOne
    @JoinColumn(name = "tagged_user_id")
    private User taggedUser;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "timestamp", name = "created_at")
    private LocalDateTime createdAt;
}
