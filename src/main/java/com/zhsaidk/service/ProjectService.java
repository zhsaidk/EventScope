package com.zhsaidk.service;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.repo.ApiKeyRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.dto.BuildProjectDTO;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ApiKeyRepository apiKeyRepository;

    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    public ResponseEntity<?> getById(Integer id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Transactional
    public ResponseEntity<Project> build(BuildProjectDTO dto){
        Project savedProject = projectRepository.save(Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.getActive())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    public ResponseEntity<?> modify(Integer id, BuildProjectDTO dto){
        return projectRepository.findById(id)
                .map(project -> {
                    project.setName(dto.getName());
                    project.setDescription(dto.getDescription());
                    project.setActive(dto.getActive());
                    projectRepository.save(project);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(project);
                }).orElseGet(()->ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public Boolean remove(Integer id){
        return projectRepository.findById(id)
                .map(project -> {
                    projectRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }


}
