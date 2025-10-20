package org.example.lab2.repository;

import org.example.lab2.entity.Topic;

import java.util.List;
import java.util.Optional;

public interface TopicRepository {
    List<Topic> findAll();
    List<Topic> findAllDeleted(Boolean deleted);
    List<Topic> findAllPinned(Boolean pinned);
    List<Topic> findAllClosed(Boolean closed);
    List<Topic> findAllByTitle(String title);
    List<Topic> findAllByAuthor(String author);

    Optional<Topic> findById(Long id);

    boolean existsById(Long id);

    Topic save(Topic topic);
    List<Topic> saveAll(List<Topic> topics);

    Topic update(Topic topic);

    int deleteById(Long id);
    int delete(Topic topic);
    int deleteAllByIds(List<Long> ids);
    int deleteAll(List<Topic> topics);
    int deleteAll();
}
