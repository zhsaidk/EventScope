package com.zhsaidk.service;

import com.zhsaidk.database.entity.*;
import com.zhsaidk.database.repo.ApiKeyRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.BuildProjectDTO;
import com.zhsaidk.util.SlugUtil;
import jakarta.annotation.PostConstruct;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public PagedModel<Project> getAll(PageRequest pageRequest, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return new PagedModel<>(projectRepository.findAllByUserIdAndAnyRole(pageRequest, userId));
    }

    public Page<Project> getAllProjects(PageRequest pageRequest, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return projectRepository.findAllByUserIdAndAnyRole(pageRequest, userId);
    }

    public ResponseEntity<?> getByProjectSlug(String projectSlug, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!permissionService.hasAnyPermission(projectSlug, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have permission");
        }

        return projectRepository.findProjectBySlug(projectSlug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Transactional
    public ResponseEntity<Project> build(BuildProjectDTO dto, Authentication authentication) {
        User user = userRepository.findUserByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Project savedProject = projectRepository.save(Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .owner(user)
                .active(dto.getActive())
                .build());

        ProjectPermission permission = ProjectPermission.builder()
                .user(user)
                .project(savedProject)
                .permission(PermissionRole.OWNER)
                .build();
        permissionService.save(permission);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    public ResponseEntity<?> modify(String projectSlug,
                                    BuildProjectDTO dto,
                                    Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER, PermissionRole.WRITER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you have not permission");
        }

        return projectRepository.findProjectBySlug(projectSlug)
                .map(project -> {
                    project.setName(dto.getName());
                    project.setDescription(dto.getDescription());
                    project.setActive(dto.getActive());
                    project.setSlug(project.getSlug());
                    projectRepository.save(project);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(project);
                }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Transactional
    public Boolean remove(String projectSlug, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if(!permissionService.hasPermission(projectSlug, userId, List.of(PermissionRole.OWNER))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can delete");
        };

        return projectRepository.findProjectBySlug(projectSlug)
                .map(project -> {
                    projectRepository.deleteBySlug(projectSlug);
                    return true;
                })
                .orElse(false);
    }

    public Project getProjectBySlug(String projectSlug, Authentication authentication) {
        Integer userId = userRepository.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if(!permissionService.hasAnyPermission(projectSlug, userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you not have any permissions");
        };

        return projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    public void save(Project project) {
        projectRepository.save(project);
    }
}
