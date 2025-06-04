package com.zhsaidk.service;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.CatalogSpecification;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.BuildCreateCatalogDto;
import com.zhsaidk.dto.BuildReadCatalogDto;
import com.zhsaidk.dto.CachedPage;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;
    private final PermissionService permissionService;
    private final RedisCacheService cacheService;
    private static final Duration CACHE_TTL = Duration.ofMillis(20000);
    private final EntityManager entityManager;

    public Page<Catalog> findAllCatalogs(PageRequest pageRequest, String projectSlug, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String generatedName = generateName(pageRequest);

        CachedPage<Catalog> catalog = cacheService.loadCachedPage(generatedName, Catalog.class, authentication);

        if (catalog != null) {
            return new PageImpl<>(catalog.getContent(), pageRequest, catalog.getTotalElements());
        }

        Page<Catalog> allCatalogs = catalogRepository.findAll(CatalogSpecification.getAll(projectSlug, userDetails.getId()), pageRequest);
        cacheService.putCachedPage(generatedName, allCatalogs, Catalog.class, authentication, CACHE_TTL);
        return allCatalogs;
    }

    public String generateName(PageRequest pageRequest) {
        if (pageRequest == null){
            throw new IllegalStateException("PageRequest must be not null");
        }
        return String.format("page_%s::size_%s",
                pageRequest.getPageNumber(), pageRequest.getPageSize());
    }

    public ResponseEntity<?> findById(String projectSlug, String catalogSlug, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Catalog cachedCatalog = cacheService.get(catalogSlug, Catalog.class, authentication);
        if (cachedCatalog != null) {
            return ResponseEntity.ok(cachedCatalog);
        }

        Catalog catalog = catalogRepository.findOne(CatalogSpecification.findCatalogByCatalogSlug(projectSlug, catalogSlug, userDetails.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        cacheService.put(catalogSlug, catalog, authentication, CACHE_TTL);
        return ResponseEntity.ok(catalog);
    }

    @Transactional
    public ResponseEntity<?> build(BuildCreateCatalogDto dto, String projectSlug, Authentication authentication) {

        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Catalog savedCatalog = catalogRepository.save(Catalog.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .project(project)
                .active(dto.getActive())
                .version(dto.getVersion())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCatalog);
    }

    @Transactional
    public ResponseEntity<?> update(String projectSlug, String catalogSlug, BuildReadCatalogDto dto, Authentication authentication) {

        if (!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)) {
            return ResponseEntity.badRequest().build();
        }

        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or writer can change");
        }

        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found"));

        return catalogRepository.findCatalogBySlug(catalogSlug)
                .map(current -> {
                    current.setName(dto.getName());
                    current.setDescription(dto.getDescription());
                    current.setActive(dto.getActive());
                    current.setProject(project);
                    current.setVersion(dto.getVersion());
                    current.setSlug(current.getSlug());
                    Catalog savedCatalog = catalogRepository.save(current);
                    cacheService.put(current.getSlug(), savedCatalog, authentication, CACHE_TTL);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(savedCatalog);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Transactional
    public Boolean remove(String projectSlug, String catalogSlug, Authentication authentication) {

        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner or writer can delete");
        }

        int deleted = catalogRepository.deleteBySlug(catalogSlug);
        if (deleted > 0) {
            cacheService.delete(catalogSlug, Catalog.class, authentication);
            return true;
        }
        return false;
    }

    public Catalog findCatalogByCatalogSlug(String projectSlug, String catalogSlug, Authentication authentication) {
        if (!permissionService.hasAnyPermission(projectSlug, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not permission");
        }
        return catalogRepository.findCatalogBySlug(catalogSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "catalog not found"));
    }
}
