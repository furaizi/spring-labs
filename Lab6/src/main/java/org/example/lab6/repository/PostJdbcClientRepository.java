package org.example.lab6.repository;

import org.example.lab6.entity.Post;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
public class PostJdbcClientRepository implements PostRepository {

    private static final String BASE_SELECT = """
            SELECT id, author_id, topic_id, title, content, likes, created_at, updated_at
            FROM posts
            """;

    private final JdbcClient jdbcClient;

    public PostJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Post> findByTopicId(UUID topicId) {
        return jdbcClient.sql(BASE_SELECT + " WHERE topic_id = :topicId ORDER BY created_at DESC")
                .param("topicId", topicId)
                .query(this::mapRow)
                .list();
    }

    @Override
    public List<Post> findByAuthorId(UUID authorId) {
        return jdbcClient.sql(BASE_SELECT + " WHERE author_id = :authorId ORDER BY created_at DESC")
                .param("authorId", authorId)
                .query(this::mapRow)
                .list();
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return jdbcClient.sql(BASE_SELECT + " WHERE id = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    @Override
    public Post save(Post post) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = post.getCreatedAt() != null ? post.getCreatedAt() : now;
        LocalDateTime updatedAt = post.getUpdatedAt() != null ? post.getUpdatedAt() : now;

        return jdbcClient.sql("""
                        INSERT INTO posts (id, author_id, topic_id, title, content, likes, created_at, updated_at)
                        VALUES (COALESCE(:id, gen_random_uuid()), :authorId, :topicId, :title, :content,
                                COALESCE(:likes, 0), :createdAt, :updatedAt)
                        ON CONFLICT (id) DO UPDATE
                          SET author_id = EXCLUDED.author_id,
                              topic_id = EXCLUDED.topic_id,
                              title = EXCLUDED.title,
                              content = EXCLUDED.content,
                              likes = EXCLUDED.likes,
                              updated_at = EXCLUDED.updated_at
                        RETURNING id, author_id, topic_id, title, content, likes, created_at, updated_at
                        """)
                .param("id", post.getId())
                .param("authorId", post.getAuthorId())
                .param("topicId", post.getTopicId())
                .param("title", post.getTitle())
                .param("content", post.getContent())
                .param("likes", post.getLikes())
                .param("createdAt", createdAt)
                .param("updatedAt", updatedAt)
                .query(this::mapRow)
                .single();
    }

    @Override
    public boolean deleteById(UUID id) {
        int updated = jdbcClient.sql("DELETE FROM posts WHERE id = :id")
                .param("id", id)
                .update();
        return updated > 0;
    }

    @Override
    public List<Post> findByTitleContaining(String keyword) {
        return jdbcClient.sql(BASE_SELECT + " WHERE LOWER(title) LIKE LOWER(:pattern) ORDER BY created_at DESC")
                .param("pattern", "%" + keyword + "%")
                .query(this::mapRow)
                .list();
    }

    @Override
    public List<Post> findAll() {
        return jdbcClient.sql(BASE_SELECT + " ORDER BY created_at DESC")
                .query(this::mapRow)
                .list();
    }

    @Override
    public boolean incrementLikes(UUID id) {
        return jdbcClient.sql("""
                        UPDATE posts
                           SET likes = likes + 1,
                               updated_at = NOW()
                         WHERE id = :id
                        """)
                .param("id", id)
                .update() > 0;
    }

    @Override
    public boolean decrementLikes(UUID id) {
        return jdbcClient.sql("""
                        UPDATE posts
                           SET likes = GREATEST(0, likes - 1),
                               updated_at = NOW()
                         WHERE id = :id AND likes > 0
                        """)
                .param("id", id)
                .update() > 0;
    }

    @Override
    public boolean update(UUID id, String newTitle, String newContent) {
        return jdbcClient.sql("""
                        UPDATE posts
                           SET title = :title,
                               content = :content,
                               updated_at = NOW()
                         WHERE id = :id
                        """)
                .param("id", id)
                .param("title", newTitle)
                .param("content", newContent)
                .update() > 0;
    }

    private Post mapRow(ResultSet rs, int rowNum) throws SQLException {
        Post p = new Post();
        p.setId(rs.getObject("id", UUID.class));
        p.setAuthorId(rs.getObject("author_id", UUID.class));
        p.setTopicId(rs.getObject("topic_id", UUID.class));
        p.setTitle(rs.getString("title"));
        p.setContent(rs.getString("content"));
        p.setLikes(rs.getInt("likes"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return p;
    }
}
