package org.example.lab4.controller.api;

import com.github.fge.jsonpatch.JsonPatch;
import org.example.lab4.dto.PostForm;
import org.example.lab4.entity.Post;
import org.example.lab4.service.api.PostApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostApiController {
    private final PostApiService service;

    public PostApiController(PostApiService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Post> create(@RequestBody PostForm form) {
        Post created = service.create(form);
        return ResponseEntity
                .created(URI.create("/api/posts/" + created.getId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAll(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findAll(title, page, size));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Post> getOne(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> put(@PathVariable UUID id, @RequestBody PostForm form) {
        return service.update(id, form)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<Post> patch(@PathVariable UUID id, @RequestBody JsonPatch patch) {
        return service.patch(id, patch)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = service.deleteById(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
