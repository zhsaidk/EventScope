package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Project;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {
    Optional<Project> findProjectBySlug(String slug);

    boolean existsBySlug(@Length(max = 255) String slug);

    @Modifying
    int deleteBySlug(@Length(max = 255) String slug);

    @EntityGraph(attributePaths = {"owner"})
    @Query("select p from Project p join p.permissions pp where pp.user.id = :userId AND pp.permission in ('OWNER', 'WRITER', 'READ')")
    Page<Project> findAllByUserIdAndAnyRole(Pageable pageable, Integer userId);
}
