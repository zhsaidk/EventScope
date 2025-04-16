package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Project;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Optional<Project> findProjectBySlug(String slug);

    boolean existsBySlug(@Length(max = 255) String slug);

    void deleteBySlug(@Length(max = 255) String slug);
}
