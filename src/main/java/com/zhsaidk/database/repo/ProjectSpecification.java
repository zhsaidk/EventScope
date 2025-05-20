package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.entity.ProjectPermission;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecification {
    public static Specification<Project> getAllProjects(Integer ownerId) {
        return ((root, query, criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            Join<Project, ProjectPermission> permissions = root.join("permissions");
            predicates.add(criteriaBuilder.equal(permissions.get("user").get("id"), ownerId));
            predicates.add(permissions.get("permission").in("OWNER", "READ", "WRITER"));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    public static Specification<Project> getProjectByProjectSlug(String projectSlug, Integer currentUserId){
        return ((root, query, criteriaBuilder) -> {
            if (query!=null){
                query.distinct(true);
            }

            if (projectSlug ==null || currentUserId == null){
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            Join<Project, ProjectPermission> permissions = root.join("permissions", JoinType.INNER);
            predicates.add(criteriaBuilder.equal(root.get("slug"), projectSlug));
            predicates.add(criteriaBuilder.equal(permissions.get("user").get("id"), currentUserId));
            predicates.add(permissions.get("permission").in("WRITER", "READ", "OWNER"));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
