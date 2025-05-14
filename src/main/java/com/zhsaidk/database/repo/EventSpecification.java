package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.Project;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.security.Permission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> byCriteria(String text, LocalDateTime begin, LocalDateTime end, Integer userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по тексту
            if (text != null && !text.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + text.toLowerCase() + "%"));
            }

            if (begin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("localCreatedAt"), begin));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("localCreatedAt"), end));
            }

            // Фильтр по userId и permission
            Join<Event, Catalog> catalog = root.join("catalog");
            Join<Catalog, Project> project = catalog.join("project");
            Join<Project, Permission> permissions = project.join("permissions");
            predicates.add(cb.equal(permissions.get("user").get("id"), userId));
            predicates.add(permissions.get("permission").in("OWNER", "READ", "WRITER"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
