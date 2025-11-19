package org.example.lab5.service;

import lombok.RequiredArgsConstructor;
import org.example.lab5.entity.Post;
import org.example.lab5.entity.Topic;
import org.example.lab5.repository.TopicRepository;
import org.springframework.stereotype.Service;

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


    public Topic getWithPosts(UUID id) {
        Topic t = getById(id);
        List<Post> posts = postService.getPostsByTopicId(id);
        t.setPosts(posts);
        return t;
    }

    public Post addPostToTopic(UUID topicId, Post post) {
        post.setTopicId(topicId);
        Post created = postService.createPost(post);
        touchReply(topicId, created.getCreatedAt() != null ? created.getCreatedAt() : LocalDateTime.now());
        return created;
    }

    public boolean deletePostFromTopic(UUID topicId, UUID postId) {
        boolean ok = postService.deletePost(postId);
        if (ok) {
            Topic t = getById(topicId);
            int replies = t.getReplyCount() == null ? 0 : t.getReplyCount();
            t.setReplyCount(Math.max(0, replies - 1));
            t.setUpdatedAt(LocalDateTime.now());
            topicRepository.update(t);
        }
        return ok;
    }




}
