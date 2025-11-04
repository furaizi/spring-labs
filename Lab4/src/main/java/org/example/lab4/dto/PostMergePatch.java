package org.example.lab4.dto;

import java.util.UUID;

public record PostMergePatch(
        UUID topicId,
        String title,
        String content
) {}