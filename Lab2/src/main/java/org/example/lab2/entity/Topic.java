package org.example.lab2.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class Topic {
    private UUID id;
    private String title;
    private String description;
    private String author;
    private Integer viewCount;
    private Integer replyCount;
    private Boolean pinned;
    private Boolean closed;
    private Set<String> tags;
    private Boolean deleted;
    private LocalDateTime lastPostAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Post> posts;
}
