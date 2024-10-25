package com.example.snapheal.responses;

import lombok.*;

import java.sql.Timestamp;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhotoResponse {
    private Long id;
    private String photoUrl;
    private Long createAt;
}
