package com.zhsaidk.service;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.BuildCreateCatalogDto;
import com.zhsaidk.dto.BuildReadCatalogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public ResponseEntity<?> build(BuildCreateCatalogDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        Catalog savedCatalog = catalogRepository.save(Catalog.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .project(project)
                .active(dto.getActive())
                .version(dto.getVersion())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCatalog);
    }

    public ResponseEntity<?> update(Integer id, BuildReadCatalogDto dto) {

        return catalogRepository.findById(id)
                .map(catalog -> {
                    catalog.setName(dto.getName());
                    catalog.setDescription(dto.getDescription());
                    catalog.setActive(dto.getActive());
                    catalog.setProject(catalog.getProject());
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
