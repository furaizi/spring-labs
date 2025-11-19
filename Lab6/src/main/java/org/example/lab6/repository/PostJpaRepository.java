package org.example.lab6.repository;

import org.example.lab6.entity.Post;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
public class PostJpaRepository implements PostRepository {

    private final SpringDataPostRepository repository;

    public PostJpaRepository(SpringDataPostRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Post> findByTopicId(UUID topicId) {
        return repository.findByTopicIdOrderByCreatedAtDesc(topicId);
    }

    @Override
    public List<Post> findByAuthorId(UUID authorId) {
        return repository.findByAuthorId(authorId);
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Post save(Post post) {
        return repository.save(post);
    }

    @Override
    @Transactional
    public boolean deleteById(UUID id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Override
    public List<Post> findByTitleContaining(String keyword) {
        return repository.searchByTitle(keyword);
    }

    @Override
    public List<Post> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public boolean incrementLikes(UUID id) {
        return repository.incrementLikes(id) > 0;
    }

    @Override
    @Transactional
    public boolean decrementLikes(UUID id) {
        return repository.decrementLikes(id) > 0;
    }

    @Override
    @Transactional
    public boolean update(UUID id, String newTitle, String newContent) {
        return repository.updateContent(id, newTitle, newContent) > 0;
    }
}
