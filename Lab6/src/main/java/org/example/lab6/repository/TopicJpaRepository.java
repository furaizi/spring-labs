package org.example.lab6.repository;

import org.example.lab6.entity.Topic;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Repository
@Primary
public class TopicJpaRepository implements TopicRepository {

    private final SpringDataTopicRepository repository;

    public TopicJpaRepository(SpringDataTopicRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Topic> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Topic> findAllDeleted(Boolean deleted) {
        return repository.findAllDeleted(deleted);
    }

    @Override
    public List<Topic> findAllPinned(Boolean pinned) {
        return repository.findAllPinned(pinned);
    }

    @Override
    public List<Topic> findAllClosed(Boolean closed) {
        return repository.findAllByClosedOrderByCreatedAtDesc(closed);
    }

    @Override
    public List<Topic> findAllByTitle(String title) {
        return repository.findAllByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
    }

    @Override
    public List<Topic> findAllByAuthor(String author) {
        return repository.findAllByAuthorContainingIgnoreCaseOrderByCreatedAtDesc(author);
    }

    @Override
    public Optional<Topic> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public Topic save(Topic topic) {
        return repository.save(topic);
    }

    @Override
    @Transactional
    public List<Topic> saveAll(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return List.of();
        }
        List<Topic> saved = new ArrayList<>();
        repository.saveAll(topics).forEach(saved::add);
        return saved;
    }

    @Override
    @Transactional
    public Topic update(Topic topic) {
        return repository.save(topic);
    }

    @Override
    @Transactional
    public int deleteById(UUID id) {
        if (!repository.existsById(id)) {
            return 0;
        }
        repository.deleteById(id);
        return 1;
    }

    @Override
    @Transactional
    public int delete(Topic topic) {
        if (topic == null || topic.getId() == null) {
            return 0;
        }
        return deleteById(topic.getId());
    }

    @Override
    @Transactional
    public int deleteAllByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<Topic> existing = StreamSupport.stream(repository.findAllById(ids).spliterator(), false)
                .toList();
        if (existing.isEmpty()) {
            return 0;
        }
        repository.deleteAll(existing);
        return existing.size();
    }

    @Override
    @Transactional
    public int deleteAll(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return 0;
        }
        List<UUID> ids = topics.stream()
                .filter(Objects::nonNull)
                .map(Topic::getId)
                .filter(Objects::nonNull)
                .toList();
        return deleteAllByIds(ids);
    }

    @Override
    @Transactional
    public int deleteAll() {
        long count = repository.count();
        repository.deleteAll();
        return (int) count;
    }
}
