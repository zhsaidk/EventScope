package com.zhsaidk.service;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.BuildCreateCatalogDto;
import com.zhsaidk.dto.BuildReadCatalogDto;
import com.zhsaidk.util.SlugUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;

    public ResponseEntity<List<Catalog>> findAll() {
        return ResponseEntity.ok(catalogRepository.findAll());
    }

    public ResponseEntity<?> findById(String projectSlug, String catalogSlug) {
        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog not fount"));

        if (project == null || catalog == null || !Objects.equals(catalog.getProject().getId(), project.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(catalog);
    }

    public ResponseEntity<?> build(BuildCreateCatalogDto dto, String projectSlug) {
        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        String baseSlug = SlugUtil.toSlug(dto.getName());
        String slug = baseSlug;
        int counter = 1;

        while (projectRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        Catalog savedCatalog = catalogRepository.save(Catalog.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .project(project)
                .slug(slug)
                .active(dto.getActive())
                .version(dto.getVersion())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCatalog);
    }

    public ResponseEntity<?> update(String projectSlug, String catalogSlug, BuildReadCatalogDto dto) {
        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (project == null || catalog == null || !Objects.equals(catalog.getProject().getId(), project.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return catalogRepository.findCatalogBySlug(catalogSlug)
                .map(current -> {
                    current.setName(dto.getName());
                    current.setDescription(dto.getDescription());
                    current.setActive(dto.getActive());
                    current.setProject(project);
                    current.setVersion(dto.getVersion());
                    current.setSlug(current.getSlug());
                    Catalog savedCatalog = catalogRepository.save(current);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(savedCatalog);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Transactional
    public Boolean remove(String catalogSlug) {
        if (catalogRepository.existsBySlug(catalogSlug)) {
            catalogRepository.deleteBySlug(catalogSlug);
            return true;
        }
        return false;
    }

    public Catalog findCatalogBySlug(String slug) {
        return catalogRepository.findCatalogBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog not found"));
    }
}
