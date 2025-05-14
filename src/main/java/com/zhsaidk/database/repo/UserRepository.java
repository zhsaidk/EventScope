package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findUserByUsername(String username);

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Optional<Integer> findUserIdByUsername(@Param("username") String username);


    boolean existsByUsername(String username);
}
