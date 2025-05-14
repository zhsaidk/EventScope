package com.zhsaidk.service;

import com.zhsaidk.database.entity.Catalog;
import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.entity.ProjectPermission;
import com.zhsaidk.database.repo.CatalogRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.BuildCreateCatalogDto;
import com.zhsaidk.dto.BuildReadCatalogDto;
import com.zhsaidk.util.SlugUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;
    private final ProjectRepository projectRepository;
    private final PermissionService permissionService;
    private final UserRepository userRepository;

    public PagedModel<Catalog> findAll(PageRequest pageRequest, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return new PagedModel<>(catalogRepository.findAllByUserIdAndAnyRole(pageRequest, userId));
    }

    public Page<Catalog> findAllCatalogs(PageRequest pageRequest, Authentication authentication){
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return catalogRepository.findAllByUserIdAndAnyRole(pageRequest, userId);
    }

    public ResponseEntity<?> findById(String projectSlug, String catalogSlug, Authentication authentication) {

        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasAnyPermission(projectSlug, userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if(!catalogRepository.existsBySlugAndProjectSlug(catalogSlug, projectSlug)){
            return ResponseEntity.badRequest().build();
        }

        Catalog catalog = catalogRepository.findCatalogBySlug(catalogSlug)
                .orElse(null);
        return ResponseEntity.ok(catalog);
    }

    public ResponseEntity<?> build(BuildCreateCatalogDto dto, String projectSlug, Authentication authentication) {

        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

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

        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or writer can change");
        }

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
    public Boolean remove(String projectSlug, String catalogSlug, Authentication authentication) {

        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner or writer can delete");
        }

        if (catalogRepository.existsBySlug(catalogSlug)) {
            catalogRepository.deleteBySlug(catalogSlug);
            return true;
        }
        return false;
    }

    public List<Catalog> findAllCatalogsByProjectSlug(String projectSlug, Authentication authentication){
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasAnyPermission(projectSlug, userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return catalogRepository.findAllCatalogsByProjectSlug(projectSlug);
    }

    public Catalog findCatalogByCatalogSlug(String catalogSlug){
        return catalogRepository.findCatalogBySlug(catalogSlug)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "catalog not found"));
    }
}
