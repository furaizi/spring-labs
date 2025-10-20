package org.example.lab2.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Post {
    private int id;
    private int authorId;
    private String title;
    private String content;
    private int likes = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
