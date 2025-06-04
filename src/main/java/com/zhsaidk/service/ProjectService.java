package com.zhsaidk.service;

import com.zhsaidk.database.entity.*;
import com.zhsaidk.database.repo.ApiKeyRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.database.repo.ProjectSpecification;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.BuildProjectDTO;
import com.zhsaidk.dto.CachedPage;
import com.zhsaidk.util.SlugUtil;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final RedisCacheService cacheService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    @PersistenceContext
    private EntityManager entityManager;
    private static final Duration CACHE_TTL = Duration.ofMillis(20000);

    public Page<Project> getAll(PageRequest pageRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String key = generateKey(pageRequest);
        CachedPage<Project> pageFromCache = cacheService.loadCachedPage(key, Project.class, authentication);
        if (pageFromCache != null) {
            return new PageImpl<>(pageFromCache.getContent(), pageRequest, pageFromCache.getTotalElements());
        }

        Page<Project> allProjects = projectRepository.findAll(ProjectSpecification.getAllProjects(userDetails.getId()), pageRequest);

        cacheService.putCachedPage(key, allProjects, Project.class, authentication, CACHE_TTL);
        return allProjects;
    }

    public String generateKey(PageRequest pageRequest) {
        if (pageRequest == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        return String.format("page_%d::size_%d",
                pageRequest.getPageNumber(), pageRequest.getPageSize());
    }

    public Project getProjectByProjectSlug(String projectSlug, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Project cachedProject = cacheService.get(projectSlug, Project.class, authentication);
        if (cachedProject != null) {
            return cachedProject;
        }

        Project project = projectRepository.findOne(ProjectSpecification.getProjectByProjectSlug(projectSlug, userDetails.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        cacheService.put(projectSlug, project, authentication, CACHE_TTL);
        return project;
    }

    @Transactional
    public Project build(BuildProjectDTO dto, Authentication authentication) {
        User user = userRepository.findUserByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .owner(user)
                .active(dto.getActive())
                .build();

        ProjectPermission permission = ProjectPermission.builder()
                .user(user)
                .permission(PermissionRole.OWNER)
                .build();

        project.addPermission(permission);
        return projectRepository.save(project);
    }

    public Project modify(String projectSlug,
                          BuildProjectDTO dto,
                          Authentication authentication) {
        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER, PermissionRole.WRITER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not permission");
        }

        Project currentProject = projectRepository.findProjectBySlug(projectSlug)
                .map(project -> {
                    project.setName(dto.getName());
                    project.setDescription(dto.getDescription());
                    project.setActive(dto.getActive());
                    project.setSlug(project.getSlug());
                    projectRepository.save(project);
                    return project;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_MODIFIED));
        cacheService.put(currentProject.getSlug(), currentProject, authentication, CACHE_TTL);
        return currentProject;
    }

    @Transactional
    public Boolean remove(String projectSlug, Authentication authentication) {
        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can delete");
        }
        int deleted = projectRepository.deleteBySlug(projectSlug);
        if (deleted > 0) {
            cacheService.delete(projectSlug, Project.class, authentication);
            return true;
        }
        return false;
    }
}
