package org.example.lab2.service;

import lombok.RequiredArgsConstructor;
import org.example.lab2.entity.Topic;
import org.example.lab2.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    public List<Topic> findByTitle(String title) {
        return topicRepository.findAllByTitle(title);
    }

    public List<Topic> findByAuthor(String author) {
        return topicRepository.findAllByAuthor(author);
    }

    public boolean existsById(UUID id) {
        return topicRepository.existsById(id);
    }

    public Topic getById(UUID id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + id));
    }

    public Topic save(Topic topic) {
        return topicRepository.save(topic);
    }

    public Topic update(Topic topic) {
        return topicRepository.update(topic);
    }

    public void deleteById(UUID id) {
        topicRepository.deleteById(id);
    }

    public void touchReply(UUID topicId, LocalDateTime postTime) {
        Topic topic = getById(topicId);
        int replies = topic.getReplyCount() == null ? 0 : topic.getReplyCount();
        topic.setReplyCount(replies + 1);
        topic.setLastPostAt(postTime);
        topicRepository.update(topic);
    }

    public Topic softDelete(UUID id) {
        Topic topic = getById(id);
        topic.setDeleted(true);
        return topicRepository.update(topic);
    }

    public Topic restore(UUID id) {
        Topic topic = getById(id);
        topic.setDeleted(false);
        return topicRepository.update(topic);
    }

    public Topic togglePinned(UUID id) {
        Topic topic = getById(id);
        topic.setPinned(topic.getPinned() == null ? true : !topic.getPinned());
        return topicRepository.update(topic);
    }

    public void incrementViewCount(UUID topicId) {
        Topic t = getById(topicId);
        t.setViewCount(t.getViewCount() == null ? 1 : t.getViewCount() + 1);
        topicRepository.update(t);
    }

    public List<Topic> restoreAllDeleted() {
        List<Topic> deleted = topicRepository.findAllDeleted(true);
        deleted.forEach(t -> t.setDeleted(false));
        return topicRepository.saveAll(deleted);
    }






}
