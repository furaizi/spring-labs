package org.example.lab6.controller.api;

import jakarta.validation.Valid;
import org.example.lab6.dto.TopicCreateRequest;
import org.example.lab6.dto.TopicUpdateRequest;
import org.example.lab6.entity.Post;
import org.example.lab6.entity.Topic;
import org.example.lab6.service.TopicService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/topics")
public class TopicApiController {

    private final TopicService topicService;

    public TopicApiController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Topic>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(required = false) Boolean closed,
            @RequestParam(required = false) Boolean deleted
    ) {
        List<Topic> filtered = topicService.findAll();
        if (title != null || author != null || pinned != null || closed != null || deleted != null) {
            filtered = filtered.stream()
                    .filter(t -> title == null || (t.getTitle() != null && t.getTitle().toLowerCase().contains(title.toLowerCase())))
                    .filter(t -> author == null || (t.getAuthor() != null && t.getAuthor().equalsIgnoreCase(author)))
                    .filter(t -> pinned == null || pinned.equals(t.getPinned()))
                    .filter(t -> closed == null || closed.equals(t.getClosed()))
                    .filter(t -> deleted == null || deleted.equals(t.getDeleted()))
                    .toList();
        }
        return ResponseEntity.ok(filtered);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Topic> getOne(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(topicService.getWithPosts(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Topic> create(@Valid @RequestBody TopicCreateRequest req) {
        Topic t = new Topic();
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setAuthor(req.author() != null ? req.author() : "system");
        t.setPinned(Boolean.TRUE.equals(req.pinned()));
        t.setClosed(Boolean.TRUE.equals(req.closed()));
        t.setTags(cleanTags(req.tags()));
        Topic saved = topicService.save(t);
        return ResponseEntity.created(URI.create("/api/v1/topics/" + saved.getId())).body(saved);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Topic> update(@PathVariable UUID id, @Valid @RequestBody TopicUpdateRequest req) {
        try {
            Topic topic = topicService.getById(id);
            topic.setTitle(req.title());
            topic.setDescription(req.description());
            if (req.pinned() != null) topic.setPinned(req.pinned());
            if (req.closed() != null) topic.setClosed(req.closed());
            if (req.tags() != null) topic.setTags(cleanTags(req.tags()));
            if (req.deleted() != null) topic.setDeleted(req.deleted());
            Topic updated = topicService.update(topic);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!topicService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        topicService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/posts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> addPost(@PathVariable UUID id, @RequestBody Post post) {
        if (!topicService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        post.setCreatedAt(LocalDateTime.now());
        Post created = topicService.addPostToTopic(id, post);
        return ResponseEntity.created(URI.create("/api/v1/posts/" + created.getId())).body(created);
    }

    private Set<String> cleanTags(Set<String> tags) {
        if (tags == null) return Set.of();
        return tags.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).collect(java.util.stream.Collectors.toSet());
    }
}
