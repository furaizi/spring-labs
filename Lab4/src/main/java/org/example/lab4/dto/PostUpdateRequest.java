package org.example.lab4.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record PostUpdateRequest(
        UUID topicId,
        @NotBlank String title,
        @NotBlank String content
) {}
