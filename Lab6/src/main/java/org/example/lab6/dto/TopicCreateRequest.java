package org.example.lab6.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record TopicCreateRequest(
        @NotBlank String title,
        String description,
        String author,
        Boolean pinned,
        Boolean closed,
        Set<String> tags
) {}
