package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Event;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> byCriteria(String name, LocalDateTime begin, LocalDateTime end) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicated = new ArrayList<>();

            if (name != null) {
                predicated.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (begin != null) {
                predicated.add(criteriaBuilder.greaterThanOrEqualTo(root.get("localCreatedAt"), begin));
            }
            if (end != null) {
                predicated.add(criteriaBuilder.lessThanOrEqualTo(root.get("localCreatedAt"), end));
            }

            return criteriaBuilder.and(predicated.toArray(new Predicate[0]));
        });
    }
}
