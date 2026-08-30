package com.learn2play.backend.service;

import com.learn2play.backend.dto.LeaderboardResponse;
import com.learn2play.backend.entity.AppUser;
import com.learn2play.backend.entity.QuizAttempt;
import com.learn2play.backend.repository.AppUserRepository;
import com.learn2play.backend.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final AppUserRepository appUserRepository;

    public LeaderboardService(
            QuizAttemptRepository quizAttemptRepository,
            AppUserRepository appUserRepository
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<LeaderboardResponse> getLeaderboard() {

        // Get ALL users from the database
        List<AppUser> users = appUserRepository.findAll();

        // Get ALL quiz attempts
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();

        // Group quiz attempts by user email
        Map<String, List<QuizAttempt>> grouped =
                attempts.stream()
                        .collect(Collectors.groupingBy(
                                QuizAttempt::getUserEmail
                        ));

        List<LeaderboardResponse> leaderboard =
                new ArrayList<>();

        // Loop through EVERY registered user
        for (AppUser user : users) {

            String email = user.getEmail();

            // Get this user's attempts.
            // If they have no quizzes, use an empty list.
            List<QuizAttempt> userAttempts =
                    grouped.getOrDefault(
                            email,
                            new ArrayList<>()
                    );

            double average =
                    userAttempts.stream()
                            .mapToDouble(
                                    QuizAttempt::getScorePercent
                            )
                            .average()
                            .orElse(0);

            double highest =
                    userAttempts.stream()
                            .mapToDouble(
                                    QuizAttempt::getScorePercent
                            )
                            .max()
                            .orElse(0);

            int quizzes =
                    userAttempts.size();

            leaderboard.add(
                    new LeaderboardResponse(
                            0,
                            user.getName(),
                            email,
                            Math.round(average * 100) / 100.0,
                            Math.round(highest * 100) / 100.0,
                            quizzes
                    )
            );
        }

        // Sort users by average score, highest first
        leaderboard.sort(
                Comparator.comparingDouble(
                        LeaderboardResponse::getAverageScore
                ).reversed()
        );

        // Assign ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        return leaderboard;
    }
}