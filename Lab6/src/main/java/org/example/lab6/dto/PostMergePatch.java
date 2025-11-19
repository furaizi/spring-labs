package org.example.lab6.dto;

import java.util.UUID;

public record PostMergePatch(
        UUID topicId,
        String title,
        String content
) {}