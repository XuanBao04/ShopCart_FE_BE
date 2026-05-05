package com.shopcart.user.repository;
import com.shopcart.common.repository.BaseRepository;

import com.shopcart.user.entity.User;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
