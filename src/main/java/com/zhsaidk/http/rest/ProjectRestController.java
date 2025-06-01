package com.zhsaidk.http.rest;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.dto.*;
import com.zhsaidk.service.CatalogService;
import com.zhsaidk.service.EventService;
import com.zhsaidk.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3")
@RequiredArgsConstructor
public class ProjectRestController {
    private final ProjectService projectService;
    private final CatalogService catalogService;
    private final EventService eventService;
    private final CatalogRepository catalogRepository;

    @GetMapping("/projects")
    public ResponseEntity<Page<Project>> getAllProject(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                             @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                             Authentication authentication) {
        return ResponseEntity.ok(projectService.getAll(PageRequest.of(page, size, Sort.by("createdAt")), authentication));
    }

    @GetMapping("/{projectSlug}")
    public ResponseEntity<?> getProjectById(@PathVariable("projectSlug") String projectSlug,
                                            Authentication authentication) {
        return ResponseEntity.ok(projectService.getProjectByProjectSlug(projectSlug, authentication));
    }

    @PostMapping("/projects")
    public ResponseEntity<?> createProject(@Valid @RequestBody BuildProjectDTO dto,
                                          Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.build(dto, authentication));
    }

    @PutMapping("/{projectSlug}")
    public ResponseEntity<?> updateProject(@PathVariable("projectSlug") String projectSlug,
                                           @Valid @RequestBody BuildProjectDTO dto,
                                           Authentication authentication) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(projectService.modify(projectSlug, dto, authentication));
    }

    @DeleteMapping("/{projectSlug}")
    public ResponseEntity<?> deleteProject(@PathVariable("projectSlug") String projectSlug,
                                           Authentication authentication) {
        return projectService.remove(projectSlug, authentication)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // TODO ----------------------Для Категории ------------------------------


    @GetMapping("/projects/catalogs")
    public ResponseEntity<Page<Catalog>> getAllCatalogs(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                              @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                              @RequestParam(value = "projectSlug", required = false) String projectSlug,
                                                              Authentication authentication) {
        return ResponseEntity.ok(catalogService.findAllCatalogs(PageRequest.of(page, size, Sort.by("createdAt")), projectSlug, authentication));
    }

    @GetMapping("/{projectSlug}/{catalogSlug}")
    public ResponseEntity<?> getCatalogById(
            @PathVariable("projectSlug") String projectSlug,
            @PathVariable("catalogSlug") String catalogSlug,
            Authentication authentication) {
        return catalogService.findById(projectSlug, catalogSlug, authentication);
    }

    @PostMapping("/{projectSlug}/catalogs")
    public ResponseEntity<?> createCatalog(@Valid @RequestBody BuildCreateCatalogDto dto,
                                          @PathVariable("projectSlug") String projectSlug,
                                          Authentication authentication) {
        return catalogService.build(dto, projectSlug, authentication);
    }

    @PutMapping("/{projectSlug}/{catalogSlug}")
    public ResponseEntity<?> updateCatalog(@PathVariable("projectSlug") String projectSlug,
                                    @PathVariable("catalogSlug") String catalogSlug,
                                    @Valid @RequestBody BuildReadCatalogDto dto,
                                    Authentication authentication) {
        return catalogService.update(projectSlug, catalogSlug, dto, authentication);
    }

    @DeleteMapping("/{projectSlug}/{catalogSlug}")  //todo
    public ResponseEntity<?> deleteCatalog(@PathVariable("projectSlug") String projectSlug,
                                           @PathVariable("catalogSlug") String catalogSlug,
                                           Authentication authentication) {

        return catalogService.remove(projectSlug, catalogSlug, authentication)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }


    //TODO ---------------------Для Events--------------------------

    @PostMapping("/{projectSlug}/{catalogSlug}/events")
    public ResponseEntity<?> createEvent(@Valid @RequestBody BuildEventDto dto,
                                        @PathVariable(value = "projectSlug") String projectSlug,
                                        @PathVariable(value = "catalogSlug") String catalogSlug,
                                        Authentication authentication) {
        return eventService.build(dto, projectSlug, catalogSlug, authentication);
    }

    @DeleteMapping("/{projectSlug}/{catalogSlug}/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable("projectSlug") String projectSlug,
                                         @PathVariable("catalogSlug") String catalogSlug,
                                         @PathVariable("eventId") UUID eventId,
                                         Authentication authentication) {

        return eventService.remove(projectSlug, catalogSlug, eventId, authentication)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{projectSlug}/{catalogSlug}/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable("projectSlug") String projectSlug,
                                          @PathVariable("catalogSlug") String catalogSlug,
                                          @PathVariable("eventId") UUID eventId,
                                          Authentication authentication) {
        return eventService.getById(eventId, projectSlug, catalogSlug, authentication);
    }

    @PutMapping("/{projectSlug}/{catalogSlug}/{eventId}") //todo
    public ResponseEntity<?> updateEvent(@PathVariable("eventId") UUID eventId,
                                         @Valid @RequestBody BuildCreateEventDto dto,
                                         @PathVariable(value = "projectSlug") String projectSlug,
                                         @PathVariable(value = "catalogSlug") String catalogSlug,
                                         Authentication authentication) {
        return eventService.update(eventId, dto, projectSlug, catalogSlug, authentication);
    }

    @GetMapping("/projects/catalogs/events")
    public ResponseEntity<PagedModel<Event>> getAllEvents(@RequestParam(name = "name", required = false) String name,
                                                          @RequestParam(name = "begin", required = false) LocalDateTime begin,
                                                          @RequestParam(name = "end", required = false) LocalDateTime end,
                                                          @RequestParam(name = "page", defaultValue = "0") Integer page,
                                                          @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                          @RequestParam(name = "catalogSlug", required = false) String catalogSlug,
                                                          Authentication authentication) {
        return eventService.findByParameters(name, begin, end, page, size, catalogSlug, authentication);
    }
}
