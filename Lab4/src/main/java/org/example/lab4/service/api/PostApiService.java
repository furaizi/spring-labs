package org.example.lab4.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import org.example.lab4.dto.*;
import org.example.lab4.entity.Post;
import org.example.lab4.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class PostApiService {
    private final PostRepository repository;
    private final ObjectMapper mapper;

    public PostApiService(PostRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // CREATE
    public Post create(PostCreateRequest req) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setAuthorId(req.authorId());
        post.setTopicId(req.topicId());
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setLikes(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return repository.save(post);
    }

    public Optional<Post> findById(UUID id) {
        return repository.findById(id);
    }

    // PUT (replace)
    public Optional<Post> replace(UUID id, PostUpdateRequest req) {
        Optional<Post> existing = repository.findById(id);
        if (existing.isEmpty())
            return Optional.empty();
        Post p = existing.get();
        p.setTopicId(req.topicId());
        p.setTitle(req.title());
        p.setContent(req.content());
        p.setUpdatedAt(LocalDateTime.now());
        repository.save(p);
        return Optional.of(p);
    }
    public Optional<Post> patchJsonPatch(UUID id, JsonPatch patch) {
        try {
            Optional<Post> existing = repository.findById(id);
            if (existing.isEmpty())
                return Optional.empty();

            Post current = existing.get();
            JsonNode node = mapper.valueToTree(current);
            JsonNode patched = patch.apply(node);
            Post result = mapper.treeToValue(patched, Post.class);

            result.setId(current.getId());
            result.setAuthorId(current.getAuthorId());
            if (result.getCreatedAt() == null)
                result.setCreatedAt(current.getCreatedAt());
            if (result.getLikes() < 0)
                result.setLikes(0);

            result.setUpdatedAt(LocalDateTime.now());
            repository.save(result);
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Post> patchMerge(UUID id, PostMergePatch patch) {
        Optional<Post> existing = repository.findById(id);
        if (existing.isEmpty())
            return Optional.empty();

        Post p = existing.get();
        if (patch.topicId() != null)
            p.setTopicId(patch.topicId());
        if (patch.title() != null)
            p.setTitle(patch.title());
        if (patch.content() != null)
            p.setContent(patch.content());

        p.setUpdatedAt(LocalDateTime.now());
        repository.save(p);
        return Optional.of(p);
    }

    public boolean deleteById(UUID id) {
        Optional<Post> existing = repository.findById(id);
        if (existing.isEmpty()) return false;
        repository.deleteById(id);
        return true;
    }

    public Page<Post> findAll(
            UUID authorId,
            UUID topicId,
            String titleContains,
            Integer minLikes,
            OffsetDateTime createdAtFrom,
            OffsetDateTime createdAtTo,
            int page, int size, String sort
    ) {
        List<Post> all = repository.findAll();

        Stream<Post> s = all.stream();
        if (authorId != null)
            s = s.filter(p -> authorId.equals(p.getAuthorId()));
        if (topicId != null)
            s = s.filter(p -> topicId.equals(p.getTopicId()));
        if (titleContains != null && !titleContains.isBlank()) {
            String q = titleContains.toLowerCase();
            s = s.filter(p -> Optional.ofNullable(p.getTitle()).orElse("").toLowerCase().contains(q));
        }
        if (minLikes != null)
            s = s.filter(p -> p.getLikes() >= minLikes);
        if (createdAtFrom != null) {
            var from = createdAtFrom.toLocalDateTime();
            s = s.filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(from));
        }
        if (createdAtTo != null) {
            var to = createdAtTo.toLocalDateTime();
            s = s.filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isAfter(to));
        }

        Comparator<Post> cmp = buildComparator(sort); // createdAt, updatedAt, likes, title
        List<Post> filtered = s.sorted(cmp).toList();

        int total = filtered.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<Post> content = filtered.subList(from, to);

        int totalPages = size <= 0
                ? 1
                : (int) Math.ceil((double) total / size);

        Map<String, String> links = new LinkedHashMap<>();
        String base = "/api/v1/posts";
        String self = buildQuery(authorId, topicId, titleContains, minLikes, createdAtFrom, createdAtTo, page, size, sort);
        links.put("self", base + self);
        links.put("next", (page + 1 < totalPages)
                ? base + buildQuery(authorId, topicId, titleContains, minLikes, createdAtFrom, createdAtTo, page + 1, size, sort)
                : null);
        links.put("prev", (page - 1 >= 0 && totalPages > 0)
                ? base + buildQuery(authorId, topicId, titleContains, minLikes, createdAtFrom, createdAtTo, page - 1, size, sort)
                : null);

        return new Page<>(content, page, size, total, totalPages, normalizeSort(sort), links);
    }

    private Comparator<Post> buildComparator(String sort) {
        String field = "createdAt";
        boolean desc = true;

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            field = parts[0].trim();
            if (parts.length > 1) {
                desc = "desc".equalsIgnoreCase(parts[1].trim());
            } else {
                desc = false;
            }
        }

        Comparator<Post> c = switch (field) {
            case "updatedAt" -> Comparator.comparing(
                    Post::getUpdatedAt,
                    Comparator.nullsFirst(Comparator.naturalOrder())
            );
            case "likes" -> Comparator.comparingInt(Post::getLikes);
            case "title" -> Comparator.comparing(
                    p -> Optional.ofNullable(p.getTitle()).orElse(""),
                    String.CASE_INSENSITIVE_ORDER
            );
            default -> Comparator.comparing(
                    Post::getCreatedAt,
                    Comparator.nullsFirst(Comparator.naturalOrder())
            );
        };

        return desc ? c.reversed() : c;
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank())
            return "createdAt,desc";
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        String dir = (parts.length > 1 ? parts[1].trim().toLowerCase() : "asc");

        if (!List.of("createdAt", "updatedAt", "likes", "title").contains(field))
            field = "createdAt";
        if (!dir.equals("asc") && !dir.equals("desc"))
            dir = "asc";
        return field + "," + dir;
    }

    private String buildQuery(UUID authorId, UUID topicId, String titleContains, Integer minLikes,
                              OffsetDateTime createdAtFrom, OffsetDateTime createdAtTo,
                              int page, int size, String sort) {
        StringBuilder sb = new StringBuilder("?page=")
                .append(page)
                .append("&size=")
                .append(size);
        if (sort != null && !sort.isBlank())
            sb.append("&sort=")
                    .append(sort);
        if (authorId != null)
            sb.append("&authorId=")
                    .append(authorId);
        if (topicId != null)
            sb.append("&topicId=")
                    .append(topicId);
        if (titleContains != null && !titleContains.isBlank())
            sb.append("&titleContains=")
                    .append(titleContains.replace(" ", "%20"));
        if (minLikes != null)
            sb.append("&minLikes=")
                    .append(minLikes);
        if (createdAtFrom != null)
            sb.append("&createdAtFrom=")
                    .append(createdAtFrom);
        if (createdAtTo != null)
            sb.append("&createdAtTo=")
                    .append(createdAtTo);
        return sb.toString();
    }
}
