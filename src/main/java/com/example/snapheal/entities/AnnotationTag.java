package com.example.snapheal.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AnnotationTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "annotation_id")
    private Annotation annotation;

    @ManyToOne
    @JoinColumn(name = "tagged_user_id")
    private User taggedUser;
}
