package com.learn2play.backend.repository;

import com.learn2play.backend.entity.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppUserRepository
        extends MongoRepository<AppUser, String> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByResetToken(String resetToken);

    boolean existsByEmail(String email);
}
