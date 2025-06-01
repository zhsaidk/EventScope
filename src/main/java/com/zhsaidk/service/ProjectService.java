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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Page<Project> getAll(PageRequest pageRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String generatedName = generateKey("Project", pageRequest.getPageNumber(), pageRequest.getPageSize());
        CachedPage<Project> pageFromCache = cacheService.getPageFromCache(generatedName, Project.class);
        if (pageFromCache != null){
            return new PageImpl<>(pageFromCache.getContent(), pageRequest, pageFromCache.getTotalElements());
        }

        Page<Project> allProjects = projectRepository.findAll(ProjectSpecification.getAllProjects(userDetails.getId()), pageRequest);
        
        cacheService.putPageInCache(generatedName, allProjects, Duration.ofSeconds(10));
        return allProjects;
    }
    
    public String generateKey(String className, Integer pageNumber, Integer size){
        return className + "::page" + ":" + pageNumber + "size" + ":" + size;
    }

    public Project getProjectByProjectSlug(String projectSlug, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Project cachedProject = cacheService.get("Project", projectSlug, Project.class);
        if(cachedProject != null){
            return cachedProject;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Project project = projectRepository.findOne(ProjectSpecification.getProjectByProjectSlug(projectSlug, userDetails.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        cacheService.put(projectSlug, project, Duration.ofSeconds(10));
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
        Project savedProject = projectRepository.save(project);
        cacheService.put(savedProject.getSlug(), savedProject, Duration.ofSeconds(60));
        return savedProject;
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
        cacheService.put(currentProject.getSlug(), currentProject, Duration.ofSeconds(60));
        return currentProject;
    }

    @Transactional
    public Boolean remove(String projectSlug, Authentication authentication) {
        if (!permissionService.hasPermission(projectSlug, authentication, List.of(PermissionRole.OWNER))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can delete");
        }
        cacheService.delete("Project", projectSlug);
        return projectRepository.findProjectBySlug(projectSlug)
                .map(project -> {
                    projectRepository.deleteBySlug(projectSlug);
                    return true;
                })
                .orElse(false);
    }
}
