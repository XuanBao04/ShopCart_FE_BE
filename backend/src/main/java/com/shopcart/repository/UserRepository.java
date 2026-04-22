package com.shopcart.repository;

import com.shopcart.entity.User;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * Find user by username
     * @param username the username to search
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a username already exists
     * @param username the username to check
     * @return true if username exists
     */
    Boolean existsByUsername(String username);
}
