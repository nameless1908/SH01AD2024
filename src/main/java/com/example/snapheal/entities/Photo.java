package com.example.snapheal.entities;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

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
    private Date createAt;
}
