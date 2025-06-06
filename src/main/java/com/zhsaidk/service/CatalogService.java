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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;
    private final MutableAclService aclService;

    public PagedModel<Catalog> findAll(PageRequest pageRequest) {
        return new PagedModel<>(catalogRepository.findAll(pageRequest));
    }

    public Page<Catalog> findAllCatalogs(PageRequest pageRequest){
        return catalogRepository.findAll(pageRequest);
    }

    public ResponseEntity<?> findById(String projectSlug, String catalogSlug) {
        if(!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)){
            return ResponseEntity.badRequest().build();
        }

        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElse(null);
        return ResponseEntity.ok(catalog);
    }

    @Transactional
    public ResponseEntity<?> build(BuildCreateCatalogDto dto, String projectSlug) {
        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Catalog savedCatalog = catalogRepository.save(Catalog.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .project(project)
                .active(dto.getActive())
                .version(dto.getVersion())
                .build());

        // --- ACL иерархия: каталог наследует права от проекта ---
        try {
            ObjectIdentity catalogOid = new ObjectIdentityImpl(Catalog.class, savedCatalog.getId());
            ObjectIdentity projectOid = new ObjectIdentityImpl(Project.class, project.getId());

            MutableAcl catalogAcl = aclService.createAcl(catalogOid);
            MutableAcl projectAcl = (MutableAcl) aclService.readAclById(projectOid);

            catalogAcl.setParent(projectAcl); // установить родителя
            catalogAcl.setEntriesInheriting(true); // включить наследование

            aclService.updateAcl(catalogAcl);
        } catch (Exception e) {
            // Если ACL по какой-то причине не создался — логируем, но не ломаем процесс
            // Можно добавить кастомную обработку ошибок
            throw new RuntimeException("Failed to set ACL hierarchy", e);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCatalog);
    }

    public ResponseEntity<?> update(String projectSlug, String catalogSlug, BuildReadCatalogDto dto) {
        if(!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)){
            return ResponseEntity.badRequest().build();
        }
        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElse(null);

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

    public List<Catalog> findAllCatalogsByProjectSlug(String projectSlug){
        return catalogRepository.findAllCatalogsByProjectSlug(projectSlug);
    }
}
