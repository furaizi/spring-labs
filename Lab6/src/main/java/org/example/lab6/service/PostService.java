package org.example.lab6.service;

import org.example.lab6.entity.Post;
import org.example.lab6.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getPostsByTopicId(UUID id) {
        return postRepository.findByTopicId(id);
    }

    public List<Post> getPostsByAuthorId(UUID id) {
        return postRepository.findByAuthorId(id);
    }

    public Optional<Post> getPostById(UUID id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post createPost(Post post) {
        LocalDateTime now = LocalDateTime.now();
        if (post.getId() == null) {
            post.setId(null);
        }
        if (post.getAuthorId() == null) {
            post.setAuthorId(UUID.randomUUID());
        }
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(now);
        }
        post.setUpdatedAt(now);
        if (post.getLikes() < 0) {
            post.setLikes(0);
        }
        return postRepository.save(post);
    }

    @Transactional
    public boolean deletePost(UUID id) {
        return postRepository.deleteById(id);
    }

    public List<Post> getPostByTitle(String keyword) {
        return postRepository.findByTitleContaining(keyword);
    }

    public boolean likePost(UUID id) {
        return postRepository.incrementLikes(id);
    }

    public boolean unlikePost(UUID id) {
        return postRepository.decrementLikes(id);
    }

    @Transactional
    public boolean updatePost(UUID id, String newTitle, String newContent) {
        return postRepository.update(id, newTitle, newContent);
    }
}
