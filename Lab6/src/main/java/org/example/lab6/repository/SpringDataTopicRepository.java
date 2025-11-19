package org.example.lab6.repository;

import org.example.lab6.entity.Topic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTopicRepository extends CrudRepository<Topic, UUID> {

    // JPQL with @Query
    @Query("select t from Topic t where t.deleted = :deleted order by t.createdAt desc")
    List<Topic> findAllDeleted(@Param("deleted") Boolean deleted);

    // Named query backed by Topic.findAllPinned
    List<Topic> findAllPinned(Boolean pinned);

    // Derived queries
    List<Topic> findAllByClosedOrderByCreatedAtDesc(Boolean closed);

    List<Topic> findAllByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    List<Topic> findAllByAuthorContainingIgnoreCaseOrderByCreatedAtDesc(String author);

    List<Topic> findAllByOrderByCreatedAtDesc();
}
