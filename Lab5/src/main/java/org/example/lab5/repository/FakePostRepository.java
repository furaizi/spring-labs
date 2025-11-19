package org.example.lab5.repository;

import org.example.lab5.entity.Post;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class FakePostRepository implements PostRepository {

    private final Map<UUID, Post> posts = new ConcurrentHashMap<>();

    @Override
    public List<Post> findByTopicId(UUID topicId) {
        return posts.values().stream()
                .filter(p -> p.getTopicId() != null && p.getTopicId().equals(topicId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> findByAuthorId(UUID authorId) {
        return posts.values().stream()
                .filter(p -> p.getAuthorId() != null && p.getAuthorId().equals(authorId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return Optional.ofNullable(posts.get(id));
    }

    @Override
    public Post save(Post post) {
        if (post.getId() == null) {
            post.setId(UUID.randomUUID());
            post.setCreatedAt(LocalDateTime.now());
        }
        post.setUpdatedAt(LocalDateTime.now());
        posts.put(post.getId(), post);
        return post;
    }

    @Override
    public boolean deleteById(UUID id) {
        return posts.remove(id) != null;
    }

    @Override
    public List<Post> findByTitleContaining(String keyword) {
        return posts.values().stream()
                .filter(p -> p.getTitle() != null && p.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> findAll(){
        return new ArrayList<>(posts.values());
    }

    @Override
    public boolean incrementLikes(UUID id) {
        Post post = posts.get(id);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
            post.setUpdatedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public boolean decrementLikes(UUID id) {
        Post post = posts.get(id);
        if (post != null && post.getLikes() > 0) {
            post.setLikes(post.getLikes() - 1);
            post.setUpdatedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public boolean update(UUID id, String newTitle, String newContent) {
        Post post = posts.get(id);
        if (post != null) {
            post.setTitle(newTitle);
            post.setContent(newContent);
            post.setUpdatedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
