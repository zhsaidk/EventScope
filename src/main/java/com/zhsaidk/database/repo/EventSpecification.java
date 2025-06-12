package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.security.Permission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventSpecification {
    public static Specification<Event> byCriteria(String text, LocalDateTime begin, LocalDateTime end, Integer userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            hasText(text, begin, end, root, cb, predicates);

            Join<Event, Catalog> catalog = root.join("catalog");
            Join<Catalog, Project> project = catalog.join("project");
            Join<Project, ProjectPermission> permissions = project.join("permissions");
            predicates.add(cb.equal(permissions.get("user").get("id"), userId));
            predicates.add(permissions.get("permission").in(PermissionRole.OWNER, PermissionRole.READ, PermissionRole.WRITER));

            assert query != null;
            if (query.getResultType() != Long.class) {
                query.select(root.get("id"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> byCriteria(String text, LocalDateTime begin, LocalDateTime end, List<UUID> uuids) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (uuids != null && !uuids.isEmpty()){
                predicates.add(root.get("id").in(uuids));
            }

            hasText(text, begin, end, root, cb, predicates);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void hasText(String text, LocalDateTime begin, LocalDateTime end, Root<Event> root, CriteriaBuilder cb, List<Predicate> predicates) {
        if (StringUtils.hasText(text)) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + text.toLowerCase() + "%"));
        }

        if (begin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("localCreatedAt"), begin));
        }

        if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("localCreatedAt"), end));
        }
    }


    public static Specification<Event> findById(UUID id, String projectSlug, String catalogSlug, Integer currentUserId){
        return (root, query, criteriaBuilder) -> {
            if (query!=null){
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();

            Join<Event, Catalog> catalog = root.join("catalog");
            Join<Catalog, Project> project = catalog.join("project");
            Join<Project, ProjectPermission> permissions = project.join("permissions");

            predicates.add(criteriaBuilder.equal(root.get("id"), id));
            predicates.add(criteriaBuilder.equal(catalog.get("slug"), catalogSlug));
            predicates.add(criteriaBuilder.equal(project.get("slug"), projectSlug));
            predicates.add(permissions.get("user").get("id").in(currentUserId));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
