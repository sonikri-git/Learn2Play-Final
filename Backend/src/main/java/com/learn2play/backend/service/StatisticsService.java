package com.learn2play.backend.service;

import com.learn2play.backend.dto.StatisticsResponse;
import com.learn2play.backend.entity.AppUser;
import com.learn2play.backend.entity.QuizAttempt;
import com.learn2play.backend.repository.AppUserRepository;
import com.learn2play.backend.repository.QuizAttemptRepository;
import com.learn2play.backend.repository.UploadedDocumentRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final AppUserRepository appUserRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;

    public StatisticsService(
            AppUserRepository appUserRepository,
            QuizAttemptRepository quizAttemptRepository,
            UploadedDocumentRepository uploadedDocumentRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
    }

    public StatisticsResponse getStatistics(String email) {

        AppUser user = appUserRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Repository returns newest first
        // Copy into a new list so we can safely reverse it for the chart
        List<QuizAttempt> attempts = new ArrayList<>(
                quizAttemptRepository
                        .findByUserEmailOrderByAttemptedAtDesc(email)
        );

        // Oldest -> newest for Score Progress chart
        Collections.reverse(attempts);

        long totalQuizzes = attempts.size();

        long uploadedDocuments =
                uploadedDocumentRepository.countByUserEmail(email);

        double averageScore = attempts.stream()
                .mapToDouble(this::getActualScore)
                .average()
                .orElse(0);

        double highestScore = attempts.stream()
                .mapToDouble(this::getActualScore)
                .max()
                .orElse(0);

        double lowestScore = attempts.stream()
                .mapToDouble(this::getActualScore)
                .min()
                .orElse(0);

        long totalQuestionsAnswered = attempts.stream()
                .mapToLong(QuizAttempt::getTotalQuestions)
                .sum();

        long totalCorrectAnswers = attempts.stream()
                .mapToLong(QuizAttempt::getCorrectCount)
                .sum();

        long totalWrongAnswers = Math.max(
                0,
                totalQuestionsAnswered - totalCorrectAnswers
        );

        double accuracy = totalQuestionsAnswered == 0
                ? 0
                : ((double) totalCorrectAnswers
                        / totalQuestionsAnswered) * 100;

        // Scores for the line chart
        List<Double> scores = attempts.stream()
                .map(this::getActualScore)
                .collect(Collectors.toList());

        // Dates for the line chart
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM dd");

        List<String> dates = attempts.stream()
                .map(attempt -> {
                    if (attempt.getAttemptedAt() == null) {
                        return "";
                    }

                    return attempt.getAttemptedAt()
                            .format(formatter);
                })
                .collect(Collectors.toList());

        return new StatisticsResponse(
                user.getName(),
                user.getEmail(),
                totalQuizzes,
                uploadedDocuments,
                round(averageScore),
                round(highestScore),
                round(lowestScore),
                totalQuestionsAnswered,
                totalCorrectAnswers,
                totalWrongAnswers,
                round(accuracy),
                scores,
                dates
        );
    }

    private double getActualScore(QuizAttempt attempt) {

        // Use the saved score if available
        if (attempt.getScorePercent() > 0) {
            return attempt.getScorePercent();
        }

        // Otherwise calculate it from correct answers
        if (attempt.getTotalQuestions() > 0) {
            return ((double) attempt.getCorrectCount()
                    / attempt.getTotalQuestions()) * 100;
        }

        return 0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}