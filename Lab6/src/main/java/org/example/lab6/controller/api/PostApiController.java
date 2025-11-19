package org.example.lab6.controller.api;

import com.github.fge.jsonpatch.JsonPatch;
import jakarta.validation.Valid;
import org.example.lab6.dto.*;
import org.example.lab6.entity.Post;
import org.example.lab6.service.api.PostApiService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
public class PostApiController {
    private final PostApiService service;

    public PostApiController(PostApiService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> create(@Valid @RequestBody PostCreateRequest req) {
        Post created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/v1/posts/" + created.getId()))
                .body(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Post>> list(
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) UUID topicId,
            @RequestParam(name = "titleContains", required = false) String titleContains,
            @RequestParam(required = false) Integer minLikes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdAtTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort // e.g. "createdAt,desc"
    ) {
        return ResponseEntity.ok(
                service.findAll(authorId, topicId, titleContains, minLikes,
                        createdAtFrom, createdAtTo,
                        page, size, sort)
        );
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> getOne(@PathVariable UUID id) {
        Optional<Post> found = service.findById(id);
        return found.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> put(@PathVariable UUID id, @Valid @RequestBody PostUpdateRequest req) {
        Optional<Post> updated = service.replace(id, req);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> patchJsonPatch(@PathVariable UUID id, @RequestBody JsonPatch patch) {
        Optional<Post> updated = service.patchJsonPatch(id, patch);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> patchMerge(@PathVariable UUID id, @RequestBody PostMergePatch patch) {
        Optional<Post> updated = service.patchMerge(id, patch);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = service.deleteById(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
