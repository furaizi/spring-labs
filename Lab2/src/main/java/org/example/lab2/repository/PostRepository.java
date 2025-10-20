package org.example.lab2.repository;

import org.example.lab2.entity.Post;
import java.util.*;

public interface PostRepository {
    List<Post> findAll();
    Optional<Post> findById(UUID id);
    Post save(Post post);
    boolean deleteById(UUID id);
    List<Post> findByAuthorId(UUID authorId);
    List<Post> findByTitleContaining(String keyword);
    boolean incrementLikes(UUID id);
    boolean decrementLikes(UUID id);
    List<Post> findTopLiked(int limit);
    boolean updateContent(UUID id, String newTitle, String newContent);
}
