package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    @Query("select e from Event e where e.catalog.slug =:catalogSlug")
    List<Event> findAllEventsByCatalogSlug(String catalogSlug);

    @EntityGraph(attributePaths = {"catalog", "catalog.project", "catalog.project.permissions"})
    Page<Event> findAll(Specification<Event> specification, Pageable pageable);
}