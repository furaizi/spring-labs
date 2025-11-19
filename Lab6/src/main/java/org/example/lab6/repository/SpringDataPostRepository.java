package org.example.lab6.repository;

import org.example.lab6.entity.Post;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataPostRepository extends CrudRepository<Post, UUID> {

    // Derived query (method name resolution)
    List<Post> findByTopicIdOrderByCreatedAtDesc(UUID topicId);

    // Named query backed by Post.findByAuthorId
    List<Post> findByAuthorId(UUID authorId);

    // JPQL with @Query
    @Query("""
            select p from Post p
             where lower(p.title) like lower(concat('%', :keyword, '%'))
             order by p.createdAt desc
            """)
    List<Post> searchByTitle(@Param("keyword") String keyword);

    // Derived sorter
    List<Post> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("update Post p set p.likes = p.likes + 1, p.updatedAt = CURRENT_TIMESTAMP where p.id = :id")
    int incrementLikes(@Param("id") UUID id);

    @Modifying
    @Query("""
            update Post p
               set p.likes = case when p.likes > 0 then p.likes - 1 else 0 end,
                   p.updatedAt = CURRENT_TIMESTAMP
             where p.id = :id and p.likes > 0
            """)
    int decrementLikes(@Param("id") UUID id);

    @Modifying
    @Query("update Post p set p.title = :title, p.content = :content, p.updatedAt = CURRENT_TIMESTAMP where p.id = :id")
    int updateContent(@Param("id") UUID id, @Param("title") String title, @Param("content") String content);
}
