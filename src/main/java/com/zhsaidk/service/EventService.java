package com.zhsaidk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.EventRepository;
import com.zhsaidk.database.repo.EventSpecification;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.BuildCreateEventDto;
import com.zhsaidk.dto.BuildEventDto;
import com.zhsaidk.dto.BuildEventWebDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;
    private final MutableAclService aclService;

    @Transactional
    public ResponseEntity<?> build(BuildEventDto dto, String projectSlug, String catalogSlug) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElseThrow(() -> new IllegalArgumentException("Catalog not found"));

        Event savedEvent = eventRepository.save(Event.builder()
                .name(dto.getName())
                .parameters(dto.getParameters())
                .catalog(catalog)
                .localCreatedAt(dto.getLocalCreatedAt())
                .build());

        // ACL
        ObjectIdentity eventOid = new ObjectIdentityImpl(Event.class, savedEvent.getId());
        ObjectIdentity catalogOid = new ObjectIdentityImpl(Catalog.class, catalog.getId());

        MutableAcl eventAcl = aclService.createAcl(eventOid);
        MutableAcl catalogAcl = (MutableAcl) aclService.readAclById(catalogOid);

        eventAcl.setParent(catalogAcl);
        eventAcl.setEntriesInheriting(true);
        aclService.updateAcl(eventAcl);

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

    @Transactional
    public void createEvent(BuildEventWebDto dto, String projectSlug, String catalogSlug) {
        try {
            if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
                throw new IllegalArgumentException("Каталог не относится к проекту");
            }

            Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                    .orElseThrow(() -> new IllegalArgumentException("Catalog not found"));

            JsonNode parameters;
            try {
                parameters = objectMapper.readTree(dto.getParameters());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid JSON in parameters: " + e.getMessage());
            }

            Event savedEvent = eventRepository.save(Event.builder()
                    .name(dto.getName())
                    .parameters(parameters)
                    .catalog(catalog)
                    .localCreatedAt(dto.getLocalCreatedAt())
                    .build());

            // ACL
            ObjectIdentity eventOid = new ObjectIdentityImpl(Event.class, savedEvent.getId());
            ObjectIdentity catalogOid = new ObjectIdentityImpl(Catalog.class, catalog.getId());

            MutableAcl eventAcl = aclService.createAcl(eventOid);
            MutableAcl catalogAcl = (MutableAcl) aclService.readAclById(catalogOid);

            // Назначаем права владельцу
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            PrincipalSid ownerSid = new PrincipalSid(username);
            eventAcl.insertAce(eventAcl.getEntries().size(), BasePermission.ADMINISTRATION, ownerSid, true);

            // Устанавливаем наследование от каталога
            eventAcl.setParent(catalogAcl);
            eventAcl.setEntriesInheriting(true);
            aclService.updateAcl(eventAcl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create event: " + e.getMessage(), e);
        }
    }
}
