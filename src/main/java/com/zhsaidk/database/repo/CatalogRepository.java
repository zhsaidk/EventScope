package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Project;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Integer> , JpaSpecificationExecutor<Catalog> {
    @EntityGraph(attributePaths = {"project"})
    Optional<Catalog> findCatalogBySlug(@Length(max = 255) String slug);

    @EntityGraph(attributePaths = {"project"})
    @Query("select c from Catalog c where c.slug = :slug")
    Optional<Catalog> findCatalogBySlugWithProject(@Length(max = 255) String slug);

    boolean existsBySlug(@Length(max = 255) String slug);

    int deleteBySlug(@Length(max = 255) String slug);

    @Query("select count(c) > 0 from Catalog c where c.slug = :catalogSlug and c.project.slug = :projectSlug")
    boolean existsBySlugAndProjectSlug(String catalogSlug, String projectSlug);

    @Query("select c from Catalog c where c.project.slug =:projectSlug")
    List<Catalog> findAllCatalogsByProjectSlug(String projectSlug);

    @Query("select c from Catalog c join c.project p join p.permissions pp where pp.user.id = :userId AND pp.permission in ('OWNER', 'WRITER', 'READ')")
    Page<Catalog> findAllByUserIdAndAnyRole(Pageable pageable, Integer userId);
}
