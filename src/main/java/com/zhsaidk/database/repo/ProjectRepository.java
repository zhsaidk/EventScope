package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Project;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Optional<Project> findProjectBySlug(String slug);

    boolean existsBySlug(@Length(max = 255) String slug);

    void deleteBySlug(@Length(max = 255) String slug);

    @EntityGraph(attributePaths = {"owner"})
    @Query("select p from Project p join p.permissions pp where pp.user.id = :userId AND pp.permission in ('OWNER', 'WRITER', 'READ')")
    Page<Project> findAllByUserIdAndAnyRole(Pageable pageable, Integer userId);
}
