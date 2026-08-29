package com.learn2play.backend.dashboard;

import com.learn2play.backend.entity.AppUser;
import com.learn2play.backend.entity.Quiz;
import com.learn2play.backend.entity.QuizAttempt;
import com.learn2play.backend.entity.UploadedDocument;
import com.learn2play.backend.repository.AppUserRepository;
import com.learn2play.backend.repository.QuizAttemptRepository;
import com.learn2play.backend.repository.QuizRepository;
import com.learn2play.backend.repository.UploadedDocumentRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AppUserRepository appUserRepository;

    private final QuizAttemptRepository quizAttemptRepository;

    private final UploadedDocumentRepository uploadedDocumentRepository;

    private final QuizRepository quizRepository;

    public DashboardService(
            AppUserRepository appUserRepository,
            QuizAttemptRepository quizAttemptRepository,
            UploadedDocumentRepository uploadedDocumentRepository,
            QuizRepository quizRepository
    ) {

        this.appUserRepository = appUserRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.quizRepository = quizRepository;

    }

    public DashboardResponse getDashboard(String email) {

        AppUser user = appUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<QuizAttempt> attempts =
                quizAttemptRepository
                        .findByUserEmailOrderByAttemptedAtDesc(email);

        long totalQuizzes = attempts.size();

        long uploadedDocuments =
                uploadedDocumentRepository.countByUserEmail(email);

        double averageScore = attempts.stream()
                .mapToDouble(QuizAttempt::getScorePercent)
                .average()
                .orElse(0);

        double highestScore = attempts.stream()
                .mapToDouble(QuizAttempt::getScorePercent)
                .max()
                .orElse(0);

        double lowestScore = attempts.stream()
                .mapToDouble(QuizAttempt::getScorePercent)
                .min()
                .orElse(0);

        long totalQuestionsAnswered = attempts.stream()
                .mapToLong(QuizAttempt::getTotalQuestions)
                .sum();

        long totalCorrectAnswers = attempts.stream()
                .mapToLong(a -> {

                    if (a.getCorrectCount() > 0) {
                        return a.getCorrectCount();
                    }

                    return Math.round(
                            a.getTotalQuestions()
                                    * a.getScorePercent()
                                    / 100.0
                    );

                })
                .sum();

        long totalWrongAnswers =
                totalQuestionsAnswered - totalCorrectAnswers;

        double accuracy = totalQuestionsAnswered == 0
                ? 0
                : ((double) totalCorrectAnswers
                / totalQuestionsAnswered) * 100;

        List<Double> scores = attempts.stream()
                .map(a -> round(a.getScorePercent()))
                .collect(Collectors.toList());

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM dd");

        List<String> dates = attempts.stream()
                .map(a -> a.getAttemptedAt().format(formatter))
                .collect(Collectors.toList());

        List<RecentAttemptDto> recentAttempts =
                buildRecentAttempts(
                        quizAttemptRepository
                                .findTop5ByUserEmailOrderByAttemptedAtDesc(email)
                );

        List<AchievementDto> achievements =
                buildAchievements(
                        totalQuizzes,
                        uploadedDocuments,
                        highestScore,
                        accuracy
                );

        return new DashboardResponse(

                user.getName(),

                user.getEmail(),

                totalQuizzes,

                uploadedDocuments,

                round(averageScore),

                round(highestScore),

                round(lowestScore),

                round(accuracy),

                totalQuestionsAnswered,

                totalCorrectAnswers,

                totalWrongAnswers,

                scores,

                dates,

                recentAttempts,

                achievements

        );

    }


    // =====================================================
    // BUILD RECENT ATTEMPTS
    // =====================================================

    private List<RecentAttemptDto> buildRecentAttempts(
            List<QuizAttempt> attempts
    ) {

        List<RecentAttemptDto> recentAttempts =
                new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM dd");

        for (QuizAttempt attempt : attempts) {

            String title = resolveQuizTitle(attempt.getQuizId());

            recentAttempts.add(

                    new RecentAttemptDto(

                            title,

                            round(
                                    attempt.getScorePercent()
                            ),

                            attempt.getAttemptedAt()
                                    .format(formatter)

                    )

            );

        }

        return recentAttempts;

    }

    // Quiz and UploadedDocument are now separate documents referenced by
    // id (no more attempt.getQuiz().getUploadedDocument() object graph),
    // so we look each one up explicitly to build a friendly title.
    private String resolveQuizTitle(String quizId) {

        if (quizId == null) {
            return "Unknown Quiz";
        }

        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return "Unknown Quiz";
        }

        if (quiz.getUploadedDocumentId() != null) {
            UploadedDocument document =
                    uploadedDocumentRepository.findById(quiz.getUploadedDocumentId())
                            .orElse(null);

            if (document != null) {
                return shortenTitle(document.getOriginalFileName());
            }
        }

        return shortenTitle(quiz.getTitle());
    }

    // =====================================================
    // BUILD ACHIEVEMENTS
    // =====================================================

    private List<AchievementDto> buildAchievements(

            long totalQuizzes,

            long uploadedDocuments,

            double highestScore,

            double accuracy

    ) {

        List<AchievementDto> achievements =
                new ArrayList<>();

        achievements.add(

                new AchievementDto(

                        "🥇",

                        "First Quiz",

                        "Complete your first quiz.",

                        totalQuizzes >= 1

                )

        );

        achievements.add(

                new AchievementDto(

                        "🏆",

                        "Quiz Master",

                        "Complete 10 quizzes.",

                        totalQuizzes >= 10

                )

        );

        achievements.add(

                new AchievementDto(

                        "⭐",

                        "Perfect Score",

                        "Score 100% in a quiz.",

                        highestScore >= 100

                )

        );

        achievements.add(

                new AchievementDto(

                        "📄",

                        "Document Collector",

                        "Upload 5 study documents.",

                        uploadedDocuments >= 5

                )

        );

        achievements.add(

                new AchievementDto(

                        "🎯",

                        "Accuracy Expert",

                        "Reach 90% overall accuracy.",

                        accuracy >= 90

                )

        );

        achievements.add(

                new AchievementDto(

                        "🔥",

                        "Dedicated Learner",

                        "Complete 25 quizzes.",

                        totalQuizzes >= 25

                )

        );

        achievements.add(

                new AchievementDto(

                        "🚀",

                        "High Performer",

                        "Score above 80%.",

                        highestScore >= 80

                )

        );

        achievements.add(

                new AchievementDto(

                        "📚",

                        "Knowledge Explorer",

                        "Upload your first document.",

                        uploadedDocuments >= 1

                )

        );

        return achievements;

    }

    // =====================================================
    // SHORTEN FILE NAME
    // =====================================================

    private String shortenTitle(String title) {

        if (title == null || title.isBlank()) {

            return "Unknown Quiz";

        }

        if (title.toLowerCase().endsWith(".pdf")) {

            title = title.substring(

                    0,

                    title.length() - 4

            );

        }

        if (title.length() <= 35) {

            return title;

        }

        return title.substring(

                0,

                32

        ) + "...";

    }

    // =====================================================
    // ROUND DOUBLE
    // =====================================================

    private double round(double value) {

        return Math.round(

                value * 100.0

        ) / 100.0;

    }

}
