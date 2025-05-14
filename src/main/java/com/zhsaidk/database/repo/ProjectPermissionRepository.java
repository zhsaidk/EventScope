package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.ProjectPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectPermissionRepository extends JpaRepository<ProjectPermission, Integer> {
    Optional<ProjectPermission> findByProjectSlugAndUserId(String projectSlug, Integer userId);

    Boolean existsByProjectSlugAndUserIdAndPermission(String projectSlug, Integer userId, PermissionRole role);

    boolean existsByProjectSlugAndUserIdAndPermissionIn(String projectSlug, Integer userId, List<PermissionRole> roles);

    @Modifying
    @Query("delete from ProjectPermission pp where pp.project.id = :projectId AND pp.user.id = :userId AND pp.permission = :role")
    void deleteByProjectIdAndUserIdAndPermission(Integer projectId, Integer userId, PermissionRole role);
}
