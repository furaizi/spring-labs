package org.example.lab5.service;

import org.example.lab5.entity.Post;
import org.example.lab5.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

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

    public boolean updatePost(UUID id, String newTitle, String newContent) {
        return postRepository.update(id, newTitle, newContent);
    }
}