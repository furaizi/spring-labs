package org.example.lab5.repository;

import org.example.lab5.entity.Topic;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@Primary
public class TopicJdbcTemplateRepository implements TopicRepository {

    private static final String BASE_SELECT = """
            SELECT pk, id, title, description, author, view_count, reply_count,
                   pinned, closed, tags, deleted, last_post_at, created_at, updated_at
              FROM topics
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Topic> rowMapper = this::mapTopic;

    public TopicJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Topic> findAll() {
        return jdbcTemplate.query(BASE_SELECT + " ORDER BY created_at DESC", rowMapper);
    }

    @Override
    public List<Topic> findAllDeleted(Boolean deleted) {
        return jdbcTemplate.query(BASE_SELECT + " WHERE deleted = ? ORDER BY created_at DESC", rowMapper, deleted);
    }

    @Override
    public List<Topic> findAllPinned(Boolean pinned) {
        return jdbcTemplate.query(BASE_SELECT + " WHERE pinned = ? ORDER BY created_at DESC", rowMapper, pinned);
    }

    @Override
    public List<Topic> findAllClosed(Boolean closed) {
        return jdbcTemplate.query(BASE_SELECT + " WHERE closed = ? ORDER BY created_at DESC", rowMapper, closed);
    }

    @Override
    public List<Topic> findAllByTitle(String title) {
        String param = "%" + title + "%";
        return jdbcTemplate.query(BASE_SELECT + " WHERE title ILIKE ? ORDER BY created_at DESC",
                rowMapper,
                param);
    }

    @Override
    public List<Topic> findAllByAuthor(String author) {
        String param = "%" + author + "%";
        return jdbcTemplate.query(BASE_SELECT + " WHERE author ILIKE ? ORDER BY created_at DESC",
                rowMapper,
                param);
    }

    @Override
    public Optional<Topic> findById(UUID id) {
        List<Topic> res = jdbcTemplate.query(BASE_SELECT + " WHERE id = ?", rowMapper, id);
        return res.stream().findFirst();
    }

    @Override
    public boolean existsById(UUID id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM topics WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Topic save(Topic topic) {
        return upsert(topic);
    }

    @Override
    public List<Topic> saveAll(List<Topic> topics) {
        List<Topic> saved = new ArrayList<>();
        if (topics == null || topics.isEmpty()) {
            return saved;
        }
        for (Topic t : topics) {
            saved.add(upsert(t));
        }
        return saved;
    }

    @Override
    public Topic update(Topic topic) {
        return upsert(topic);
    }

    @Override
    public int deleteById(UUID id) {
        return jdbcTemplate.update("DELETE FROM topics WHERE id = ?", id);
    }

    @Override
    public int delete(Topic topic) {
        if (topic == null || topic.getId() == null) return 0;
        return deleteById(topic.getId());
    }

    @Override
    public int deleteAllByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM topics WHERE id = ANY(?)");
            Array uuidArray = con.createArrayOf("uuid", ids.toArray(new UUID[0]));
            ps.setArray(1, uuidArray);
            return ps;
        });
    }

    @Override
    public int deleteAll(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) return 0;
        List<UUID> ids = topics.stream()
                .filter(t -> t != null && t.getId() != null)
                .map(Topic::getId)
                .toList();
        return deleteAllByIds(ids);
    }

    @Override
    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM topics");
    }

    private Topic upsert(Topic topic) {
        if (topic == null) return null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = topic.getCreatedAt() != null ? topic.getCreatedAt() : now;
        LocalDateTime updatedAt = topic.getUpdatedAt() != null ? topic.getUpdatedAt() : now;

        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO topics (id, title, description, author, view_count, reply_count,
                                        pinned, closed, tags, deleted, last_post_at, created_at, updated_at)
                    VALUES (COALESCE(?, gen_random_uuid()), ?, ?, ?, COALESCE(?, 0), COALESCE(?, 0),
                            COALESCE(?, FALSE), COALESCE(?, FALSE), ?, COALESCE(?, FALSE), ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE
                      SET title = EXCLUDED.title,
                          description = EXCLUDED.description,
                          author = EXCLUDED.author,
                          view_count = EXCLUDED.view_count,
                          reply_count = EXCLUDED.reply_count,
                          pinned = EXCLUDED.pinned,
                          closed = EXCLUDED.closed,
                          tags = EXCLUDED.tags,
                          deleted = EXCLUDED.deleted,
                          last_post_at = EXCLUDED.last_post_at,
                          updated_at = EXCLUDED.updated_at
                    RETURNING pk, id, title, description, author, view_count, reply_count,
                              pinned, closed, tags, deleted, last_post_at, created_at, updated_at
                    """);
            ps.setObject(1, topic.getId());
            ps.setString(2, topic.getTitle());
            ps.setString(3, topic.getDescription());
            ps.setString(4, topic.getAuthor());
            ps.setObject(5, topic.getViewCount());
            ps.setObject(6, topic.getReplyCount());
            ps.setObject(7, topic.getPinned());
            ps.setObject(8, topic.getClosed());
            ps.setArray(9, toSqlArray(con, topic.getTags()));
            ps.setObject(10, topic.getDeleted());
            ps.setObject(11, topic.getLastPostAt());
            ps.setObject(12, createdAt);
            ps.setObject(13, updatedAt);
            return ps;
        }, rs -> rs.next() ? mapTopic(rs, 1) : null);
    }

    private Array toSqlArray(java.sql.Connection connection, Set<String> tags) throws SQLException {
        if (tags == null) return null;
        return connection.createArrayOf("text", tags.toArray(new String[0]));
    }

    private Topic mapTopic(ResultSet rs, int rowNum) throws SQLException {
        Topic t = new Topic();
        t.setId(rs.getObject("id", UUID.class));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setAuthor(rs.getString("author"));
        t.setViewCount(rs.getInt("view_count"));
        t.setReplyCount(rs.getInt("reply_count"));
        t.setPinned(rs.getBoolean("pinned"));
        t.setClosed(rs.getBoolean("closed"));
        t.setDeleted(rs.getBoolean("deleted"));

        Array tagsArray = rs.getArray("tags");
        if (tagsArray != null) {
            String[] raw = (String[]) tagsArray.getArray();
            Set<String> tagSet = new HashSet<>();
            for (String s : raw) {
                if (s != null) tagSet.add(s);
            }
            t.setTags(tagSet);
        }

        var lastPost = rs.getTimestamp("last_post_at");
        if (lastPost != null) t.setLastPostAt(lastPost.toLocalDateTime());

        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        t.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return t;
    }
}
