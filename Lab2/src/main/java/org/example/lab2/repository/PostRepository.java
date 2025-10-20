package org.example.lab2.repository;

import org.example.lab2.entity.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> findAll();
    Optional<Post> findById(int id);
    Post save(Post post);
    boolean deleteById(int id);
    List<Post> findByAuthorId(int authorId);
    List<Post> findByTitleContaining(String keyword);
    boolean incrementLikes(int id);
    boolean decrementLikes(int id);
    List<Post> findTopLiked(int limit);
    boolean updateContent(int id, String newTitle, String newContent);
}
