package com.zhsaidk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhsaidk.database.entity.*;
import com.zhsaidk.database.repo.*;
import com.zhsaidk.dto.BuildCreateEventDto;
import com.zhsaidk.dto.BuildEventDto;
import com.zhsaidk.dto.BuildEventWebDto;
import com.zhsaidk.dto.CachedPage;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CatalogRepository catalogRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final RedisCacheService cacheService;
    private final EntityManager entityManager;
    private static final Duration CACHE_TTL = Duration.ofMillis(20000);

    @Transactional
    public ResponseEntity<?> build(BuildEventDto dto, String projectSlug, String catalogSlug, Authentication authentication) {

        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not permission");
        }

        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElse(null);

        Event savedEvent = eventRepository.save(Event.builder()
                .name(dto.getName())
                .parameters(dto.getParameters())
                .catalog(catalog)
                .localCreatedAt(dto.getLocalCreatedAt())
                .build());
        return ResponseEntity.ok(savedEvent);
    }

    @Transactional
    public Boolean remove(String projectSlug,
                          String catalogSlug,
                          UUID id,
                          Authentication authentication) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Каталог не относится к проекту");
        }

        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        int deleted = eventRepository.deleteEventById(id);
        if (deleted>0) {
            cacheService.delete(id.toString(), Event.class, authentication);
            return true;
        }
        return false;
    }

    public ResponseEntity<?> getById(UUID id, String projectSlug, String catalogSlug, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Event cachedEvent = cacheService.get(id.toString(), Event.class, authentication);
        if (cachedEvent != null){
            return ResponseEntity.ok(cachedEvent);
        }

        Event currentEvent = eventRepository.findOne(EventSpecification.findById(id, projectSlug, catalogSlug, userDetails.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        cacheService.put(id.toString(), currentEvent, authentication, CACHE_TTL);
        return ResponseEntity.ok(currentEvent);
    }

    public ResponseEntity<?> update(UUID id, BuildCreateEventDto dto, String projectSlug, Authentication authentication) {

        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return eventRepository.findById(id)
                .map(event -> {
                    event.setName(dto.getName());
                    event.setCatalog(event.getCatalog());
                    event.setParameters(dto.getParameters());
                    event.setLocalCreatedAt(dto.getLocalCreatedAt());
                    Event savedEvent = eventRepository.save(event);
                    cacheService.put(event.getId().toString(), savedEvent, authentication, CACHE_TTL);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public String generateKey(PageRequest pageRequest){
        return String.format("page_%s::size_%s",
                pageRequest.getPageSize(), pageRequest.getPageSize());
    }

    public Page<Event> findAllEvents(PageRequest pageRequest, String text, LocalDateTime begin, LocalDateTime end, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String generatedName = generateKey(pageRequest);
        CachedPage<Event> pageFromCache = cacheService.loadCachedPage(generatedName, Event.class, authentication);
        if (pageFromCache != null){
            return new PageImpl<>(pageFromCache.getContent(), pageRequest, pageFromCache.getTotalElements());
        }

        Page<Event> allEvents = eventRepository.findAll(EventSpecification.byCriteria(text, begin, end, userDetails.getId()), pageRequest);

        cacheService.putCachedPage(generatedName, allEvents, Project.class, authentication, CACHE_TTL);

        return allEvents;
    }

    public List<Event> findEvents(String text,LocalDateTime begin,LocalDateTime end, Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return eventRepository.findAll(EventSpecification.byCriteria(text, begin, end, userDetails.getId()));
    };

    @Transactional
    public void createEvent(BuildEventWebDto dto, String projectSlug, String catalogSlug, Authentication authentication) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            throw new IllegalArgumentException("Каталог не относится к проекту");
        }

        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not permission");
        }


        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElse(null);
        JsonNode parameters;
        try {
            parameters = objectMapper.readTree(dto.getParameters());
            System.out.println("Parsed parameters: " + parameters);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON in parameters: " + e.getMessage());
        }

        Event savedEvent = eventRepository.save(Event.builder()

                .name(dto.getName())
                .parameters(parameters)
                .catalog(catalog)
                .localCreatedAt(dto.getLocalCreatedAt())
                .build());
        cacheService.put(savedEvent.getId().toString(), savedEvent, authentication, CACHE_TTL);
    }
}
