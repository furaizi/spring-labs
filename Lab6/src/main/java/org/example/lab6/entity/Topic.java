package org.example.lab6.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "topics")
@NamedQuery(
        name = "Topic.findAllPinned",
        query = "select t from Topic t where t.pinned = :pinned order by t.createdAt desc"
)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false)
    private Integer replyCount;

    @Column(nullable = false)
    private Boolean pinned;

    @Column(nullable = false)
    private Boolean closed;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private Set<String> tags;

    @Column(nullable = false)
    private Boolean deleted;

    @Column
    private LocalDateTime lastPostAt;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "topic", fetch = FetchType.LAZY)
    @OrderBy("createdAt desc")
    private List<Post> posts;
}
