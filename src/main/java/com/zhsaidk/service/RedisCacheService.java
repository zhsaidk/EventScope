package com.zhsaidk.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhsaidk.database.entity.Project;
import com.zhsaidk.dto.CachedPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void put(String key, Object object, Authentication authentication, Duration ttl) {
        try {
            String generatedName = generateName(key, object.getClass(), authentication);
            redisTemplate.opsForValue().set(generatedName, object, ttl);
        } catch (DataAccessException exception) {

            log.error("Failed to put to Redis: {}", exception.getMessage());
            throw new RuntimeException("Redis cache error: {}", exception);
        }
    }

    public <T> T get(String key, Class<T> clazz, Authentication authentication) {
        try {
            Object result = redisTemplate.opsForValue().get(generateName(key, clazz, authentication));
            if (result == null) {
                log.debug("Cache miss for key: {}:{}", clazz.getSimpleName(), key);
                return null;
            }
            return objectMapper.convertValue(result, clazz);
        } catch (DataAccessException exception) {
            log.info("Failed to get from Redis: {}", exception.getMessage());
            return null;
        }
    }

    public String generateName(String key, Class<?> clazz, Authentication authentication){
        if (key == null || clazz ==null || authentication == null){
            throw new IllegalStateException("Key, class or Authentication must not be null");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return String.format("username_%s::class_%s::key_%s",
                userDetails.getUsername(), clazz.getSimpleName(), key);
    }

    public <T> CachedPage<T> loadCachedPage(String key, Class<T> clazz, Authentication authentication) {
        try {
            String generatedName = generateName(key, clazz, authentication);
            Object currentObject = redisTemplate.opsForValue().get(generatedName);
            if (currentObject == null) {
                return null;
            }
            JavaType pageType = objectMapper.getTypeFactory().constructParametricType(CachedPage.class, clazz);
            return objectMapper.convertValue(currentObject, pageType);
        } catch (Exception exception) {
            log.error("An error occurred while deserializing the Page object: {}", exception.getMessage());
            return null;
        }
    }

    public void putCachedPage(String key, Page<?> page, Class<?> clazz, Authentication authentication, Duration ttl) {
        try {

            String generatedName = generateName(key, clazz, authentication);
            CachedPage<?> cachedPage = new CachedPage<>(page.getContent(), page.getTotalElements());
            redisTemplate.opsForValue().set(generatedName, cachedPage, ttl);
        } catch (Exception e) {
            System.err.println("An error occurred while serializing the page object  : " + e.getMessage());
        }
    }

    public void delete(String key, Class<?> clazz, Authentication authentication) {
        try {
            redisTemplate.delete(generateName(key, clazz, authentication));
        } catch (DataAccessException exception) {
            log.error("Failed to delete from Redis: {}", exception.getMessage());
        }
    }
}
