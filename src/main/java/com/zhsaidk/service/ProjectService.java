package com.zhsaidk.service;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.ApiKeyRepository;
import com.zhsaidk.database.repo.ProjectRepository;
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

    public ResponseEntity<PagedModel<Project>> getAll(PageRequest pageRequest) {
        return ResponseEntity.ok(new PagedModel<>(projectRepository.findAll(pageRequest)));
    }

    public ResponseEntity<?> getById(String projectSlug) {
        return projectRepository.findProjectBySlug(projectSlug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Transactional
    public ResponseEntity<Project> build(BuildProjectDTO dto) {
        Project savedProject = projectRepository.save(Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.getActive())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    public ResponseEntity<?> modify(String projectSlug, BuildProjectDTO dto) {

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
    public Boolean remove(String projectSlug) {
        return projectRepository.findProjectBySlug(projectSlug)
                .map(project -> {
                    projectRepository.deleteBySlug(projectSlug);
                    return true;
                })
                .orElse(false);
    }

    public Project getProjectBySlug(String slug) {
        return projectRepository.findProjectBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

}
