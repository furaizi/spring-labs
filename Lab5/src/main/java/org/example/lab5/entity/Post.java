package org.example.lab5.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Post {
    private UUID id;
    private UUID authorId;
    private UUID topicId;
    private String title;
    private String content;
    private int likes = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
