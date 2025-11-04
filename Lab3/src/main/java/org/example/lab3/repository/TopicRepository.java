package org.example.lab3.repository;

import org.example.lab3.entity.Topic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository {
    List<Topic> findAll();
    List<Topic> findAllDeleted(Boolean deleted);
    List<Topic> findAllPinned(Boolean pinned);
    List<Topic> findAllClosed(Boolean closed);
    List<Topic> findAllByTitle(String title);
    List<Topic> findAllByAuthor(String author);

    Optional<Topic> findById(UUID id);

    boolean existsById(UUID id);

    Topic save(Topic topic);
    List<Topic> saveAll(List<Topic> topics);

    Topic update(Topic topic);

    int deleteById(UUID id);
    int delete(Topic topic);
    int deleteAllByIds(List<UUID> ids);
    int deleteAll(List<Topic> topics);
    int deleteAll();
}
