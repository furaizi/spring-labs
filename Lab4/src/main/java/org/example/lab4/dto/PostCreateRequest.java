package org.example.lab4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PostCreateRequest(
        @NotNull UUID authorId,
        UUID topicId,
        @NotBlank String title,
        @NotBlank String content
) {}
