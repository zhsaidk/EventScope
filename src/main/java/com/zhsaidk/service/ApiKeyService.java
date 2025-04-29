package com.zhsaidk.service;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.ApiKeyRepository;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.CreateKeyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    public List<ApiKey> getKeys(Principal principal) {
        User user = userRepository.findUserByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return apiKeyRepository.findApiKeyByUserId(user.getId());
    }

    public boolean deleteKey(Integer id) {
        if (apiKeyRepository.existsById(id)) {
            apiKeyRepository.deleteById(id);
            return true;
        }
        return false;
    }


    public boolean setNotActive(Integer id) {
        return apiKeyRepository.findById(id)
                .map(key -> {
                    key.set_active(false);
                    apiKeyRepository.save(key);
                    return true;
                })
                .orElse(false);
    }

    public ApiKey createKey(CreateKeyDto dto, Authentication authentication){
        User user = userRepository.findUserByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        ApiKey key = ApiKey.builder()
                .key_hash(UUID.randomUUID().toString())
                .description(dto.getDescription())
                .expiresAt(LocalDateTime.now().plusHours(dto.getHours()))
                .user(user)
                .is_active(true)
                .build();
        return apiKeyRepository.save(key);
    }
}
