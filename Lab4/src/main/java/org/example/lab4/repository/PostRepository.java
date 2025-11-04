package org.example.lab4.repository;

import org.example.lab4.entity.Post;

import java.util.*;

public interface PostRepository {
    List<Post> findByTopicId(UUID topicId);
    List<Post> findByAuthorId(UUID authorId);
    Optional<Post> findById(UUID id);
    Post save(Post post);
    boolean deleteById(UUID id);
    List<Post> findByTitleContaining(String keyword);
    List<Post> findAll();
    boolean incrementLikes(UUID id);
    boolean decrementLikes(UUID id);
    boolean update(UUID id, String newTitle, String newContent);
}
