package org.example.lab5.repository;

import org.example.lab5.entity.Topic;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("fake")
public class FakeTopicRepository implements TopicRepository {

    private final Map<UUID, Topic> storage = new ConcurrentHashMap<>();

    private static LocalDateTime now() {
        return LocalDateTime.now();
    }

    private static boolean eq(Boolean a, Boolean b) {
        return Objects.equals(a, b);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    @SuppressWarnings("unused")
    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) return false;
        return haystack.toLowerCase().contains(needle.toLowerCase().trim());
    }

    private static Topic copy(Topic t) {
        if (t == null) return null;
        Topic c = new Topic();
        c.setId(t.getId());
        c.setTitle(t.getTitle());
        c.setDescription(t.getDescription());
        c.setAuthor(t.getAuthor());
        c.setViewCount(t.getViewCount());
        c.setReplyCount(t.getReplyCount());
        c.setPinned(t.getPinned());
        c.setClosed(t.getClosed());
        c.setTags(t.getTags() == null ? null : Set.copyOf(t.getTags()));
        c.setDeleted(t.getDeleted());
        c.setLastPostAt(t.getLastPostAt());
        c.setCreatedAt(t.getCreatedAt());
        c.setUpdatedAt(t.getUpdatedAt());
        return c;
    }

    private static List<Topic> copyList(Collection<Topic> list) {
        return list.stream().map(FakeTopicRepository::copy).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Topic> findAll() {
        return copyList(storage.values());
    }

    @Override
    public List<Topic> findAllDeleted(Boolean deleted) {
        var res = storage.values().stream()
                .filter(t -> eq(t.getDeleted(), deleted))
                .collect(Collectors.toList());
        return copyList(res);
    }

    @Override
    public List<Topic> findAllPinned(Boolean pinned) {
        var res = storage.values().stream()
                .filter(t -> eq(t.getPinned(), pinned))
                .collect(Collectors.toList());
        return copyList(res);
    }

    @Override
    public List<Topic> findAllClosed(Boolean closed) {
        var res = storage.values().stream()
                .filter(t -> eq(t.getClosed(), closed))
                .collect(Collectors.toList());
        return copyList(res);
    }

    @Override
    public List<Topic> findAllByTitle(String title) {
        var res = storage.values().stream()
                .filter(t -> equalsIgnoreCase(t.getTitle(), title))
                .collect(Collectors.toList());
        return copyList(res);
    }

    @Override
    public List<Topic> findAllByAuthor(String author) {
        var res = storage.values().stream()
                .filter(t -> equalsIgnoreCase(t.getAuthor(), author))
                .collect(Collectors.toList());
        return copyList(res);
    }

    @Override
    public Optional<Topic> findById(UUID id) {
        return Optional.ofNullable(copy(storage.get(id)));
    }

    @Override
    public boolean existsById(UUID id) {
        return storage.containsKey(id);
    }

    @Override
    public Topic save(Topic topic) {
        if (topic == null) return null;

        Topic toSave = copy(topic);

        if (toSave.getId() == null) {
            toSave.setId(UUID.randomUUID());
            if (toSave.getCreatedAt() == null) {
                toSave.setCreatedAt(now());
            }
        } else {
            if (toSave.getCreatedAt() == null) {
                Topic old = storage.get(toSave.getId());
                toSave.setCreatedAt(old != null && old.getCreatedAt() != null ? old.getCreatedAt() : now());
            }
        }

        toSave.setUpdatedAt(now());
        storage.put(toSave.getId(), copy(toSave));
        return copy(toSave);
    }

    @Override
    public List<Topic> saveAll(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) return Collections.emptyList();
        List<Topic> saved = new ArrayList<>(topics.size());
        for (Topic t : topics) {
            saved.add(save(t));
        }
        return saved;
    }

    @Override
    public Topic update(Topic topic) {
        if (topic == null) return null;
        UUID id = topic.getId();
        if (id == null || !storage.containsKey(id)) {
            return save(topic);
        }
        Topic toUpdate = copy(topic);
        Topic old = storage.get(id);
        if (toUpdate.getCreatedAt() == null && old != null) {
            toUpdate.setCreatedAt(old.getCreatedAt());
        }
        toUpdate.setUpdatedAt(now());
        storage.put(id, copy(toUpdate));
        return copy(toUpdate);
    }

    @Override
    public int deleteById(UUID id) {
        return storage.remove(id) != null ? 1 : 0;
    }

    @Override
    public int delete(Topic topic) {
        if (topic == null || topic.getId() == null) return 0;
        return storage.remove(topic.getId()) != null ? 1 : 0;
    }

    @Override
    public int deleteAllByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        int removed = 0;
        for (UUID id : ids) {
            if (storage.remove(id) != null) removed++;
        }
        return removed;
    }

    @Override
    public int deleteAll(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) return 0;
        int removed = 0;
        for (Topic t : topics) {
            if (t != null && t.getId() != null && storage.remove(t.getId()) != null) removed++;
        }
        return removed;
    }

    @Override
    public int deleteAll() {
        int size = storage.size();
        storage.clear();
        return size;
    }
}
