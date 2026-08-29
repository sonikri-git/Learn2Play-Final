package com.learn2play.backend.service;

import com.learn2play.backend.dto.LeaderboardResponse;
import com.learn2play.backend.entity.AppUser;
import com.learn2play.backend.entity.QuizAttempt;
import com.learn2play.backend.repository.AppUserRepository;
import com.learn2play.backend.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.util.*;
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

        List<QuizAttempt> attempts = quizAttemptRepository.findAll();

        Map<String, List<QuizAttempt>> grouped =
                attempts.stream()
                        .collect(Collectors.groupingBy(
                                QuizAttempt::getUserEmail
                        ));

        List<LeaderboardResponse> leaderboard =
                new ArrayList<>();

        for (String email : grouped.keySet()) {

            List<QuizAttempt> userAttempts =
                    grouped.get(email);

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

            String name =
                    appUserRepository
                            .findByEmail(email)
                            .map(AppUser::getName)
                            .orElse(email);

            leaderboard.add(

                    new LeaderboardResponse(

                            0,

                            name,

                            email,

                            Math.round(average * 100) / 100.0,

                            Math.round(highest * 100) / 100.0,

                            quizzes

                    )

            );

        }

        leaderboard.sort(

                Comparator.comparingDouble(
                        LeaderboardResponse::getAverageScore
                ).reversed()

        );

        for (int i = 0; i < leaderboard.size(); i++) {

            leaderboard.get(i).setRank(i + 1);

        }

        return leaderboard;

    }

}