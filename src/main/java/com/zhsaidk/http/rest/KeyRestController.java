package com.zhsaidk.http.rest;

import com.zhsaidk.dto.CreateKeyDto;
import com.zhsaidk.service.ApiKeyService;
import com.zhsaidk.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/rest")
@RequiredArgsConstructor
public class KeyRestController {
    private final ApiKeyService apiKeyService;

    @DeleteMapping("/key/{keyId}")
    public ResponseEntity<?> deleteKey(@PathVariable("keyId") Integer keyId){
        return apiKeyService.deleteKey(keyId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/key/{keyId}")
    public ResponseEntity<?> setNotActive(@PathVariable("keyId") Integer keyId,
                                          Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (!Objects.equals(userDetails.getId(), keyId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return apiKeyService.setNotActive(keyId)
                ? ResponseEntity.status(HttpStatus.ACCEPTED).build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/api")
    public ResponseEntity<?> createKey(@RequestBody CreateKeyDto keyDto,
                                       Authentication authentication){
        return ResponseEntity.ok(apiKeyService.createKey(keyDto, authentication));
    }
}
