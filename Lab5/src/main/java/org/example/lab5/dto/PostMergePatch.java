package org.example.lab5.dto;

import java.util.UUID;

public record PostMergePatch(
        UUID topicId,
        String title,
        String content
) {}