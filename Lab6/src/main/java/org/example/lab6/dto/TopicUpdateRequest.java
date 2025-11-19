package org.example.lab6.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record TopicUpdateRequest(
        @NotBlank String title,
        String description,
        Boolean pinned,
        Boolean closed,
        Set<String> tags,
        Boolean deleted
) {}
