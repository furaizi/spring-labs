package org.example.lab5.service;

import lombok.RequiredArgsConstructor;
import org.example.lab5.entity.Post;
import org.example.lab5.entity.Topic;
import org.example.lab5.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final PostService postService;

    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Topic> findByTitle(String title) {
        if (title == null || title.isBlank()) {
            return findAll();
        }
        return topicRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    public List<Topic> findByAuthor(String author) {
        if (author == null || author.isBlank()) {
            return findAll();
        }
        return topicRepository.findAllByAuthor(author);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return topicRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public Topic getById(UUID id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + id));
    }

    @Transactional
    public Topic save(Topic topic) {
        LocalDateTime now = LocalDateTime.now();
        if (topic.getAuthor() == null) {
            topic.setAuthor("system");
        }
        if (topic.getCreatedAt() == null) {
            topic.setCreatedAt(now);
        }
        topic.setUpdatedAt(now);
        if (topic.getReplyCount() == null) {
            topic.setReplyCount(0);
        }
        if (topic.getViewCount() == null) {
            topic.setViewCount(0);
        }
        if (topic.getPinned() == null) {
            topic.setPinned(false);
        }
        if (topic.getClosed() == null) {
            topic.setClosed(false);
        }
        if (topic.getDeleted() == null) {
            topic.setDeleted(false);
        }
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic update(Topic topic) {
        topic.setUpdatedAt(LocalDateTime.now());
        return topicRepository.update(topic);
    }

    @Transactional
    public void deleteById(UUID id) {
        topicRepository.deleteById(id);
    }

    @Transactional
    public void touchReply(UUID topicId, LocalDateTime postTime) {
        Topic topic = getById(topicId);
        int replies = topic.getReplyCount() == null ? 0 : topic.getReplyCount();
        topic.setReplyCount(replies + 1);
        topic.setLastPostAt(postTime);
        update(topic);
    }

    @Transactional
    public Topic softDelete(UUID id) {
        Topic topic = getById(id);
        topic.setDeleted(true);
        return topicRepository.update(topic);
    }

    @Transactional
    public Topic restore(UUID id) {
        Topic topic = getById(id);
        topic.setDeleted(false);
        return topicRepository.update(topic);
    }

    @Transactional
    public Topic togglePinned(UUID id) {
        Topic topic = getById(id);
        topic.setPinned(topic.getPinned() == null ? true : !topic.getPinned());
        return update(topic);
    }

    @Transactional
    public void incrementViewCount(UUID topicId) {
        Topic t = getById(topicId);
        t.setViewCount(t.getViewCount() == null ? 1 : t.getViewCount() + 1);
        update(t);
    }

    @Transactional
    public List<Topic> restoreAllDeleted() {
        List<Topic> deleted = topicRepository.findAllDeleted(true);
        deleted.forEach(t -> t.setDeleted(false));
        return topicRepository.saveAll(deleted);
    }


    @Transactional(readOnly = true)
    public Topic getWithPosts(UUID id) {
        Topic t = getById(id);
        List<Post> posts = postService.getPostsByTopicId(id);
        t.setPosts(posts);
        return t;
    }

    @Transactional
    public Post addPostToTopic(UUID topicId, Post post) {
        post.setTopicId(topicId);
        Post created = postService.createPost(post);
        touchReply(topicId, created.getCreatedAt() != null ? created.getCreatedAt() : LocalDateTime.now());
        return created;
    }

    @Transactional
    public void movePost(UUID fromTopicId, UUID toTopicId, LocalDateTime postTime) {
        if (fromTopicId != null && !fromTopicId.equals(toTopicId) && existsById(fromTopicId)) {
            Topic from = getById(fromTopicId);
            int replies = from.getReplyCount() == null ? 0 : from.getReplyCount();
            from.setReplyCount(Math.max(0, replies - 1));
            update(from);
        }
        if (toTopicId != null && !toTopicId.equals(fromTopicId)) {
            touchReply(toTopicId, postTime != null ? postTime : LocalDateTime.now());
        }
    }

    @Transactional
    public boolean deletePostFromTopic(UUID topicId, UUID postId) {
        boolean ok = postService.deletePost(postId);
        if (ok) {
            Topic t = getById(topicId);
            int replies = t.getReplyCount() == null ? 0 : t.getReplyCount();
            t.setReplyCount(Math.max(0, replies - 1));
            t.setUpdatedAt(LocalDateTime.now());
            update(t);
        }
        return ok;
    }




}
