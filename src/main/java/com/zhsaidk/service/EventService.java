package com.zhsaidk.service;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.EventRepository;
import com.zhsaidk.database.repo.EventSpecification;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.BuildCreateEventDto;
import com.zhsaidk.dto.BuildEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;

    public ResponseEntity<?> build(BuildEventDto dto, String projectSlug, String catalogSlug) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElse(null); // теперь это безопаснее, потому что уже проверили

        Event savedEvent = eventRepository.save(Event.builder()
                .name(dto.getName())
                .parameters(dto.getParameters())
                .catalog(catalog)
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

    public ResponseEntity<?> update(UUID id, BuildCreateEventDto dto) {

        return eventRepository.findById(id)
                .map(event -> {
                    event.setName(dto.getName());
                    event.setCatalog(event.getCatalog());
                    event.setParameters(dto.getParameters());
                    event.setLocalCreatedAt(dto.getLocalCreatedAt());
                    Event savedEvent = eventRepository.save(event);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<PagedModel<Event>> findByParameters(String text, LocalDateTime begin, LocalDateTime end, Integer pageNumber, Integer size) {
        Page<Event> page = eventRepository.findAll(EventSpecification.byCriteria(text, begin, end), PageRequest.of(pageNumber, size, Sort.by("localCreatedAt")));
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    public List<Event> findAllEventsByCatalogSlug(String catalogSlug){
        return eventRepository.findAllEventsByCatalogSlug(catalogSlug);
    }

    public Page<Event> findAllEvents(PageRequest pageRequest, String text, LocalDateTime begin, LocalDateTime end){
        return eventRepository.findAll(EventSpecification.byCriteria(text, begin, end), pageRequest);
    }
}
