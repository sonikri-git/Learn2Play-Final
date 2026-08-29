package com.learn2play.backend.service;

import com.learn2play.backend.dto.ProfileResponse;
import com.learn2play.backend.entity.AppUser;
import com.learn2play.backend.entity.QuizAttempt;
import com.learn2play.backend.repository.AppUserRepository;
import com.learn2play.backend.repository.QuizAttemptRepository;
import com.learn2play.backend.repository.UploadedDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final AppUserRepository appUserRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;

    public ProfileService(
            AppUserRepository appUserRepository,
            QuizAttemptRepository quizAttemptRepository,
            UploadedDocumentRepository uploadedDocumentRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
    }

    public ProfileResponse getProfile(String email) {

        AppUser user = appUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<QuizAttempt> attempts =
                quizAttemptRepository.findByUserEmailOrderByAttemptedAtDesc(email);

        long totalQuizzes = attempts.size();

        double averageScore = 0;

        double highestScore = 0;

        if (!attempts.isEmpty()) {

            averageScore = attempts.stream()

                    .mapToDouble(QuizAttempt::getScorePercent)

                    .average()

                    .orElse(0);

            highestScore = attempts.stream()

                    .mapToDouble(QuizAttempt::getScorePercent)

                    .max()

                    .orElse(0);

        }

        long uploadedDocuments =
                uploadedDocumentRepository.countByUserEmail(email);

        return new ProfileResponse(

                user.getName(),

                user.getEmail(),

                totalQuizzes,

                Math.round(averageScore * 100.0) / 100.0,

                Math.round(highestScore * 100.0) / 100.0,

                uploadedDocuments

        );

    }

}