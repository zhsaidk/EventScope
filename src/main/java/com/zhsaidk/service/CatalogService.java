package com.zhsaidk.service;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.BuildCatalogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;

    public ResponseEntity<List<Catalog>> getAll() {
        return ResponseEntity.ok(catalogRepository.findAll());
    }

    public ResponseEntity<?> findById(Integer id) {
        return catalogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<?> build(BuildCatalogDto dto) {
        Optional<Project> project = projectRepository.findById(dto.getProjectId());

        if (project.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Project with " + dto.getProjectId() + " not found");
        }

        Catalog savedCatalog = catalogRepository.save(Catalog.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .project(project.get())
                .active(dto.getActive())
                .version(dto.getVersion())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCatalog);
    }

    public ResponseEntity<?> update(Integer id, BuildCatalogDto dto) {
        Optional<Project> project = projectRepository.findById(dto.getProjectId());
        if (project.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project with " + dto.getProjectId() + " not found");
        }

        return catalogRepository.findById(id)
                .map(catalog -> {
                    catalog.setName(dto.getName());
                    catalog.setDescription(dto.getDescription());
                    catalog.setActive(dto.getActive());
                    catalog.setProject(project.get());
                    catalog.setVersion(dto.getVersion());
                    Catalog savedCatalog = catalogRepository.save(catalog);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(savedCatalog);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public Boolean remove(Integer id) {
        if (catalogRepository.existsById(id)) {
            catalogRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
