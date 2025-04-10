package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE " +
            "((:name IS NULL) OR (LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))) " +
            "AND ((CAST(:begin AS TIMESTAMP) IS NULL) OR (e.localCreatedAt >= :begin)) " +
            "AND ((CAST(:end AS TIMESTAMP) IS NULL) OR (e.localCreatedAt <= :end))")
    List<Event> findEventsByCriteria(
            @Param("name") String name,
            @Param("begin") Timestamp begin,
            @Param("end") Timestamp end
    );
}