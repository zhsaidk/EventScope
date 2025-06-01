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
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void put(String key, Object value, Duration ttl) {
        try {
            String keyValue = value.getClass().getSimpleName() + "::" + key;
            redisTemplate.opsForValue().set(keyValue, value, ttl);
        } catch (DataAccessException exception) {

            log.error("Failed to put to Redis: {}", exception.getMessage());
            throw new RuntimeException("Redis cache error: {}", exception);
        }
    }

    public <T> T get(String className, String key, Class<T> clazz) {
        try {
            Object result = redisTemplate.opsForValue().get(className + "::" + key);
            if (result == null) {
                log.debug("Cache miss for key: {}:{}", className, key);
                return null;
            }
            return objectMapper.convertValue(result, clazz);
        } catch (DataAccessException exception) {
            log.info("Failed to get from Redis: {}", exception.getMessage());
            return null;
        }
    }

    public <T> CachedPage<T> getPageFromCache(String key, Class<T> clazz) {
        try {
            Object currentObject = redisTemplate.opsForValue().get(key);
            if (currentObject == null) {
                return null;
            }
            JavaType pageType = objectMapper.getTypeFactory().constructParametricType(CachedPage.class, clazz);
            return objectMapper.convertValue(currentObject, pageType);
        } catch (Exception e) {
            return null;
        }
    }

    public void putPageInCache(String key, Page<?> page, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, new CachedPage<>(page.getContent(), page.getTotalElements()), ttl);
        } catch (Exception e) {
            System.err.println("Ошибка сериализации Page: " + e.getMessage());
        }
    }

    public void delete(String className, String key) {
        try {
            if (redisTemplate.opsForValue().get(className + "::" + key) != null){
                redisTemplate.delete(className + "::" + key);
            };
        } catch (DataAccessException exception) {
            log.error("Failed to delete from Redis: {}", exception.getMessage());
        }
    }
}
