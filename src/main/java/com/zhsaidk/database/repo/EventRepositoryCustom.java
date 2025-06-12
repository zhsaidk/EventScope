package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public interface EventRepositoryCustom {
    Page<UUID> findIdsBySpecification(Specification<Event> spec, Pageable pageable);
}
