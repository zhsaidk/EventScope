package com.zhsaidk.service;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.EventRepository;
import com.zhsaidk.dto.BuildEventDto;
import com.zhsaidk.dto.SearchEventsDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CatalogRepository catalogRepository;

    public ResponseEntity<?> build(BuildEventDto dto) {
        Optional<Catalog> catalog = catalogRepository.findById(dto.getCatalogId());

        if (catalog.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Catalog with id: " + dto.getCatalogId() + " not found");
        }

        Event savedEvent = eventRepository.save(Event.builder()
                .name(dto.getName())
                .parameters(dto.getParameters())
                .catalog(catalog.get())
                .localCreatedAt(dto.getLocalCreatedAt())
                .build());

        return ResponseEntity.ok(savedEvent);
    }

    public Boolean remove(UUID id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ResponseEntity<?> getById(UUID id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<?> update(UUID id, BuildEventDto dto) {
        Optional<Catalog> catalog = catalogRepository.findById(dto.getCatalogId());

        if (catalog.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Catalog with catalogId: " + dto.getCatalogId() + " not found");
        }

        return eventRepository.findById(id)
                .map(event -> {
                    event.setName(dto.getName());
                    event.setCatalog(catalog.get());
                    event.setParameters(dto.getParameters());
                    event.setLocalCreatedAt(dto.getLocalCreatedAt());
                    Event savedEvent = eventRepository.save(event);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<List<Event>> findByParameters(SearchEventsDto dto) {
        List<Event> result = eventRepository.findEventsByCriteria(dto.getName(), dto.getBegin(), dto.getEnd());
        return ResponseEntity.ok(result);
    }
}
