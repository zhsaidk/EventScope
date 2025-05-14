package com.zhsaidk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public ResponseEntity<?> build(BuildEventDto dto, String projectSlug, String catalogSlug, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not permission");
        }

        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElse(null);// теперь это безопаснее, потому что уже проверили

        Event savedEvent = eventRepository.save(Event.builder()
                .name(dto.getName())
                .parameters(dto.getParameters())
                .catalog(catalog)
                .localCreatedAt(dto.getLocalCreatedAt())
                .build());

        return ResponseEntity.ok(savedEvent);
    }


    public Boolean remove(String projectSlug,
                          String catalogSlug,
                          UUID id,
                          Authentication authentication) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Каталог не относится к проекту");
        }

        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ResponseEntity<?> getById(UUID id, String projectSlug, String catalogSlug, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        if (!permissionService.hasAnyPermission(projectSlug, userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<?> update(UUID id, BuildCreateEventDto dto, String projectSlug, String catalogSlug, Authentication authentication) {

        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

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
//TODO нужно разобратся чтобы каждый пользователь видел свои Евенты
    public ResponseEntity<PagedModel<Event>> findByParameters(String text, LocalDateTime begin, LocalDateTime end, Integer pageNumber, Integer size, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        Page<Event> page = eventRepository.findAll(EventSpecification.byCriteria(text, begin, end, userId), PageRequest.of(pageNumber, size, Sort.by("localCreatedAt")));
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    public Page<Event> findAllEvents(PageRequest pageRequest, String text, LocalDateTime begin, LocalDateTime end, Authentication authentication){
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return eventRepository.findAll(EventSpecification.byCriteria(text, begin, end, userId), pageRequest);
    }

    public List<Event> findAllEventsByCatalogSlug(String catalogSlug, Authentication authentication){
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        Catalog catalog = catalogRepository.findCatalogBySlugWithProject(catalogSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog not found"));

        if (!permissionService.hasAnyPermission(catalog.getProject().getSlug(), userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not any permission");
        }


        return eventRepository.findAllEventsByCatalogSlug(catalogSlug);
    }

    @Transactional
    public void createEvent(BuildEventWebDto dto, String projectSlug, String catalogSlug, Authentication authentication) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            throw new IllegalArgumentException("Каталог не относится к проекту");
        }

        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not permission");
        }


        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElse(null); // теперь это безопаснее, потому что уже проверили
        JsonNode parameters;
        try {
            parameters = objectMapper.readTree(dto.getParameters());
            System.out.println("Parsed parameters: " + parameters);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON in parameters: " + e.getMessage());
        }

        eventRepository.save(Event.builder()

                .name(dto.getName())
                .parameters(parameters)
                .catalog(catalog)
                .localCreatedAt(dto.getLocalCreatedAt())
                .build());
    }
}
