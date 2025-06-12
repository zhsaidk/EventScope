package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventRepositoryCustomImpl implements EventRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<UUID> findIdsBySpecification(Specification<Event> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Основной запрос для выборки UUID
        CriteriaQuery<UUID> query = cb.createQuery(UUID.class);
        Root<Event> root = query.from(Event.class);

        // Применяем спецификацию
        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        // Указываем, что выбираем только id
        query.select(root.get("id"));

        // Добавляем сортировку, если она указана в Pageable
        if (pageable.getSort().isSorted()) {
            query.orderBy(pageable.getSort().stream()
                    .map(order -> order.isAscending() ?
                            cb.asc(root.get(order.getProperty())) :
                            cb.desc(root.get(order.getProperty())))
                    .collect(Collectors.toList()));
        }

        // Выполняем запрос с пагинацией
        TypedQuery<UUID> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Получаем список UUID для текущей страницы
        List<UUID> content = typedQuery.getResultList();

        // Запрос для подсчета общего количества записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Event> countRoot = countQuery.from(Event.class);
        countQuery.select(cb.count(countRoot));

        // Заново применяем спецификацию для countQuery
        Predicate countPredicate = spec.toPredicate(countRoot, countQuery, cb);
        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
