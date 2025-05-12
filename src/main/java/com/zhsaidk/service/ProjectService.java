package com.zhsaidk.service;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.entity.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
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
    private final MutableAclService aclService;

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public PagedModel<Project> getAll(PageRequest pageRequest) {
        return new PagedModel<>(projectRepository.findAll(pageRequest));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public Page<Project> getAllProjects(PageRequest pageRequest) {
        return projectRepository.findAll(pageRequest);
    }

    @PreAuthorize("hasPermission(#id, 'com.zhsaidk.database.entity.Project', 'READ')")
    public ResponseEntity<?> getById(String projectSlug) {
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
                .active(dto.getActive())
                .owner(user)
                .build());

        ObjectIdentity oid = new ObjectIdentityImpl(Project.class, savedProject.getId());
        MutableAcl acl = aclService.createAcl(oid);

        Sid ownerSid = new PrincipalSid(user.getUsername());

        // Выдаем права OWNER (все разрешения)
        acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, ownerSid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.READ, ownerSid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, ownerSid, true);

        aclService.updateAcl(acl);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }


    @PreAuthorize("hasPermission(#projectSlug, 'com.zhsaidk.database.entity.Project', 'WRITE')")
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
    @PreAuthorize("hasPermission(#id, 'com.zhsaidk.database.entity.Project', 'WRITE')")
    public Boolean remove(Integer id) {
        return projectRepository.findById(id)
                .map(project -> {
                    projectRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    @PreAuthorize("hasPermission(#id, 'com.zhsaidk.database.entity.Project', 'READ')")
    public Project getProjectBySlug(String slug) {
        return projectRepository.findProjectBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    public void save(Project project) {
        projectRepository.save(project);
    }
}
