package org.example.common.repository;

import org.example.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    // для Spring Security нам нужно искать по email
    Optional<User> findByEmail(String email);
    // Optional<User> а не просто User потому что
    // пользователь может не существовать
    // Optional заставляет нас явно обработать этот случай
    // вместо того чтобы получить NullPointerException

    boolean existsByEmail(String email);
}