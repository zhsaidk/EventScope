package com.zhsaidk.http.rest;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Event;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.EventRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.*;
import com.zhsaidk.service.CatalogService;
import com.zhsaidk.service.EventService;
import com.zhsaidk.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
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
    public ResponseEntity<PagedModel<Project>> getAllProject(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                             @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(projectService.getAll(PageRequest.of(page, size, Sort.by("createdAt"))));
    }

    @GetMapping("/{projectSlug}")
    public ResponseEntity<?> getProjectById(@PathVariable("projectSlug") String projectSlug) {
        return projectService.getById(projectSlug);
    }

    @PostMapping("/projects")
    public ResponseEntity<?> buildProject(@Valid @RequestBody BuildProjectDTO dto,
                                          BindingResult bindingResult,
                                          Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return projectService.build(dto, authentication);
    }

    @PutMapping("/{projectSlug}")
    public ResponseEntity<?> modifyProject(@PathVariable("projectSlug") String projectSlug,
                                           @Valid @RequestBody BuildProjectDTO dto,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return projectService.modify(projectSlug, dto);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> removeProject(@PathVariable("projectId") Integer projectId) {
        return projectService.remove(projectId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // TODO ----------------------Для Категории ------------------------------


    @GetMapping("/projects/catalogs")
    public ResponseEntity<PagedModel<Catalog>> getAllCatalogs(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                              @RequestParam(value = "szie", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(catalogService.findAll(PageRequest.of(page, size, Sort.by("createdAt"))));
    }

    @GetMapping("/{projectSlug}/{catalogSlug}")
    public ResponseEntity<?> getCatalogById(
            @PathVariable("projectSlug") String projectSlug,
            @PathVariable("catalogSlug") String catalogSlug) {
        return catalogService.findById(projectSlug, catalogSlug);
    }

    @PostMapping("/{projectSlug}/catalogs")
    public ResponseEntity<?> buildCatalog(@Valid @RequestBody BuildCreateCatalogDto dto,
                                          BindingResult bindingResult,
                                          @PathVariable("projectSlug") String projectSlug) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }

        return catalogService.build(dto, projectSlug);
    }

    @PutMapping("/{projectSlug}/{catalogSlug}")
    public ResponseEntity<?> update(@PathVariable("projectSlug") String projectSlug,
                                    @PathVariable("catalogSlug") String catalogSlug,
                                    @Valid @RequestBody BuildReadCatalogDto dto,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
        }
        return catalogService.update(projectSlug, catalogSlug, dto);
    }

    @DeleteMapping("/{projectSlug}/{catalogSlug}")  //todo
    public ResponseEntity<?> removeCatalog(@PathVariable("projectSlug") String projectSlug,
                                           @PathVariable("catalogSlug") String catalogSlug) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }
        ;

        return catalogService.remove(catalogSlug)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }


    //TODO ---------------------Для Events--------------------------

    @PostMapping("/{projectSlug}/{catalogSlug}/events")
    public ResponseEntity<?> builtEvent(@Valid @RequestBody BuildEventDto dto,
                                        BindingResult bindingResult,
                                        @PathVariable(value = "projectSlug") String projectSlug,
                                        @PathVariable(value = "catalogSlug") String catalogSlug) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return eventService.build(dto, projectSlug, catalogSlug);
    }

    @DeleteMapping("/{projectSlug}/{catalogSlug}/{eventId}")
    public ResponseEntity<?> removeEvent(@PathVariable("projectSlug") String projectSlug,
                                         @PathVariable("catalogSlug") String catalogSlug,
                                         @PathVariable("eventId") UUID eventId) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        return eventService.remove(eventId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{projectSlug}/{catalogSlug}/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable("projectSlug") String projectSlug,
                                          @PathVariable("catalogSlug") String catalogSlug,
                                          @PathVariable("eventId") UUID eventId) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }
        return eventService.getById(eventId);
    }

    @PutMapping("/{projectSlug}/{catalogSlug}/{eventId}") //todo
    public ResponseEntity<?> updateEvent(@PathVariable("eventId") UUID eventId,
                                         @Valid @RequestBody BuildCreateEventDto dto,
                                         BindingResult bindingResult,
                                         @PathVariable(value = "projectSlug") String projectSlug,
                                         @PathVariable(value = "catalogSlug") String catalogSlug) {
        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return eventService.update(eventId, dto);
    }

    @GetMapping("/projects/catalogs/events")
    public ResponseEntity<PagedModel<Event>> getAllEvents(@RequestParam(name = "name", required = false) String name,
                                                          @RequestParam(name = "begin", required = false) LocalDateTime begin,
                                                          @RequestParam(name = "end", required = false) LocalDateTime end,
                                                          @RequestParam(name = "page", defaultValue = "0") Integer page,
                                                          @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return eventService.findByParameters(name, begin, end, page, size);
    }
}
