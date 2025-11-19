package org.example.lab5.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record PostUpdateRequest(
        UUID topicId,
        @NotBlank String title,
        @NotBlank String content
) {}
