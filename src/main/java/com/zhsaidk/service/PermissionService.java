package com.zhsaidk.service;

import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.database.entity.ProjectPermission;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.ProjectPermissionRepository;
import com.zhsaidk.database.repo.ProjectRepository;
import com.zhsaidk.database.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    private final ProjectRepository projectRepository;
    private final ProjectPermissionRepository permissionRepository;
    private final UserRepository userRepository;


    @Transactional
    public void grantPermission(String projectSlug, Integer userId, PermissionRole role, Authentication authentication){
        UserDetailsImpl owner = (UserDetailsImpl) authentication.getPrincipal();

        Project project = projectRepository.findProjectBySlug(projectSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!permissionRepository.existsByProjectSlugAndUserIdAndPermission(projectSlug, owner.getId(), PermissionRole.OWNER)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can grant permissions");
        }

        if (role == PermissionRole.OWNER){
            revokePermission(projectSlug, owner.getId(), owner.getId());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProjectPermission projectPermission = ProjectPermission.builder()
                .user(user)
                .project(project)
                .permission(role)
                .build();

        permissionRepository.save(projectPermission);
    }

    @Transactional
    public void revokePermission(String projectSlug, Integer userId, Integer ownerId){
        ProjectPermission permission = permissionRepository.findByProjectSlugAndUserId(projectSlug, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission not found"));

        if (!permissionRepository.existsByProjectSlugAndUserIdAndPermission(projectSlug, userId, PermissionRole.OWNER)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can revoke permission");
        }

        permissionRepository.delete(permission);
    }

    public boolean hasPermission(String projectSlug, Authentication authentication, List<PermissionRole> role){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return permissionRepository.existsByProjectSlugAndUserIdAndPermissionIn(projectSlug, userDetails.getId(), role);
    }

    public boolean hasAnyPermission(String projectSlug, Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return permissionRepository.existsByProjectSlugAndUserIdAndPermissionIn(projectSlug, userDetails.getId(), List.of(PermissionRole.OWNER, PermissionRole.WRITER, PermissionRole.READ));
    }

    public void save(ProjectPermission projectPermission){
        permissionRepository.save(projectPermission);
    }
}
