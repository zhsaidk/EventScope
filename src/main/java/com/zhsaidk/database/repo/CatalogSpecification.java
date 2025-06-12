package com.zhsaidk.database.repo;

import ch.qos.logback.core.util.StringUtil;
import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.entity.ProjectPermission;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CatalogSpecification {
    public static Specification<Catalog> getAll(String projectSlug, Integer currentUserId){
        return (root, query, criteriaBuilder) -> {
            queryNotNull(query);
            List<Predicate> predicates = new ArrayList<>();

            Join<Catalog, Project> projects = root.join("project", JoinType.INNER);
            Join<Project, ProjectPermission> permissions = projects.join("permissions");

            if (StringUtils.hasText(projectSlug)){
                predicates.add(criteriaBuilder.equal(projects.get("slug"), projectSlug));
            }
            predicates.add(criteriaBuilder.equal(permissions.get("user").get("id"), currentUserId));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    private static void queryNotNull(CriteriaQuery<?> query){
        if (query != null){
            query.distinct(true);
        }
    }

    public static Specification<Catalog> findCatalogByCatalogSlug(String projectSlug, String catalogSlug, Integer currentUserId){
        return (root, query, criteriaBuilder) -> {
            queryNotNull(query);
            List<Predicate> predicates = new ArrayList<>();

            Join<Catalog, Project> project = root.join("project", JoinType.INNER);
            Join<Project, ProjectPermission> permissions = project.join("permissions");

            predicates.add(criteriaBuilder.equal(root.get("slug"), catalogSlug));
            predicates.add(criteriaBuilder.equal(project.get("slug"), projectSlug));
            predicates.add(criteriaBuilder.equal(permissions.get("user").get("id"), currentUserId));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
