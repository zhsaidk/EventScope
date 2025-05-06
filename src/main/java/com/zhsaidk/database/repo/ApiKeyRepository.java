package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.ApiKey;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Integer> {
    @EntityGraph(attributePaths = {"user"})
    @Query("select a from ApiKey a where a.key_hash = :key and a.is_active = true ")
    Optional<ApiKey> findByKeyHashAndIsActiveTrue(String key);

    List<ApiKey> findApiKeyByUserId(Integer userId);
}
