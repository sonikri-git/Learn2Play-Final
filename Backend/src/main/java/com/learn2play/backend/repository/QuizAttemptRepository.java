package com.learn2play.backend.repository;

import com.learn2play.backend.entity.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {

    List<QuizAttempt> findByQuizIdOrderByAttemptedAtDesc(String quizId);

    List<QuizAttempt> findByUserEmailOrderByAttemptedAtDesc(String userEmail);

    List<QuizAttempt> findTop5ByUserEmailOrderByAttemptedAtDesc(String userEmail);

    long countByUserEmail(String userEmail);
}
