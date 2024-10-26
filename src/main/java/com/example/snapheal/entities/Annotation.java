package com.example.snapheal.entities;

import com.example.snapheal.responses.AnnotationResponse;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Annotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	private String title; //title user input
    private String name; // name of location
    private double latitude;
    private double longitude;
    private String address;
    private String thumbnail;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
	private Date createAt;
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
                .createAt(createAt.getTime())
                .updateAt(updateAt.getTime())
                .build();
    }
}
