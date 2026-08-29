package com.learn2play.backend.repository;

import com.learn2play.backend.entity.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends MongoRepository<Quiz, String> {

    Optional<Quiz> findTopByOrderByCreatedAtDesc();

    List<Quiz> findAllByOrderByCreatedAtDesc();
}
