package org.example.lab4.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.example.lab4.entity.Post;
import org.example.lab4.repository.PostRepository;
import org.example.lab4.dto.PostForm;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostApiService {
    private final PostRepository repository;
    private final ObjectMapper mapper;

    public PostApiService(PostRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Post create(PostForm form) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return repository.save(post);
    }

    public List<Post> findAll(String title, int page, int size) {
        List<Post> all = repository.findAll();

        if (title != null && !title.isBlank()) {
            all = all.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .toList();
        }

        int fromIndex = Math.min(page * size, all.size());
        int toIndex = Math.min(fromIndex + size, all.size());
        return all.subList(fromIndex, toIndex);
    }


    public Optional<Post> findById(UUID id) {
        return repository.findById(id);
    }

    public Optional<Post> update(UUID id, PostForm form) {
        Optional<Post> existing = repository.findById(id);
        if (existing.isEmpty()) return Optional.empty();

        Post post = existing.get();
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        repository.save(post);
        return Optional.of(post);
    }

    public Optional<Post> patch(UUID id, JsonPatch patch) {
        try {
            Optional<Post> existing = repository.findById(id);
            if (existing.isEmpty()) return Optional.empty();

            Post post = existing.get();

            JsonNode postNode = mapper.convertValue(post, JsonNode.class);

            JsonNode patchedNode = patch.apply(postNode);

            Post patchedPost = mapper.treeToValue(patchedNode, Post.class);

            patchedPost.setUpdatedAt(LocalDateTime.now());

            repository.save(patchedPost);
            return Optional.of(patchedPost);

        } catch (JsonPatchException | IllegalArgumentException e) {
            e.printStackTrace();
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean deleteById(UUID id) {
        Optional<Post> existing = repository.findById(id);
        if (existing.isEmpty()) return false;
        repository.deleteById(id);
        return true;
    }
}
