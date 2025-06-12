package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.ProjectPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event>, EventRepositoryCustom{

    Page<Event> findAll(Specification<Event> specification, Pageable pageable);

    @Modifying
    int deleteEventById(UUID id);

    List<Event> findAllById(Iterable<UUID> uuids);

    Page<UUID> findIdsBySpecification(Specification<Event> spec, Pageable pageable);
}