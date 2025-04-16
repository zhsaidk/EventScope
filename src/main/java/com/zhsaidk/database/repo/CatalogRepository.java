package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Catalog;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Integer> {
    Optional<Catalog> findCatalogBySlug(@Length(max = 255) String slug);

    boolean existsBySlug(@Length(max = 255) String slug);

    void deleteBySlug(@Length(max = 255) String slug);
}
