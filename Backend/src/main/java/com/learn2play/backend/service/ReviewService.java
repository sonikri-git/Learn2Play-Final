package com.learn2play.backend.service;

import com.learn2play.backend.dto.ReviewQuestionDto;
import com.learn2play.backend.dto.ReviewResponse;
import com.learn2play.backend.entity.Question;
import com.learn2play.backend.entity.Quiz;
import com.learn2play.backend.entity.QuizAttempt;
import com.learn2play.backend.entity.UserAnswer;
import com.learn2play.backend.repository.QuizAttemptRepository;
import com.learn2play.backend.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;

    public ReviewService(
            QuizAttemptRepository quizAttemptRepository,
            QuizRepository quizRepository
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizRepository = quizRepository;
    }

    public ReviewResponse getReview(
            String attemptId
    ) {

        QuizAttempt attempt =
                quizAttemptRepository
                        .findById(attemptId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Attempt not found"
                                )
                        );

        // Answers now live embedded on the attempt itself; questions live
        // embedded on the quiz, so we look the quiz up once and match
        // answers to questions by questionId.
        Quiz quiz = quizRepository
                .findById(attempt.getQuizId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Quiz not found for this attempt"
                        )
                );

        Map<String, Question> questionsById = quiz.getQuestions()
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<UserAnswer> answers = attempt.getAnswers();

        ReviewResponse response =
                new ReviewResponse();

        response.setAttemptId(
                attempt.getId()
        );

        response.setQuizId(
                quiz.getId()
        );

        response.setQuizTitle(
                quiz.getTitle()
        );

        response.setUserEmail(
                attempt.getUserEmail()
        );

        response.setTotalQuestions(
                attempt.getTotalQuestions()
        );

        response.setCorrectAnswers(
                attempt.getCorrectCount()
        );

        response.setScorePercent(
                attempt.getScorePercent()
        );

        List<ReviewQuestionDto> questionDtos =
                new ArrayList<>();

        for (UserAnswer answer : answers) {

            Question question = questionsById.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }

            ReviewQuestionDto dto =
                    new ReviewQuestionDto();

            dto.setQuestionId(
                    question.getId()
            );

            dto.setType(
                    question.getType()
            );

            dto.setQuestion(
                    question.getQuestionText()
            );

            dto.setOptionA(
                    question.getOptionA()
            );

            dto.setOptionB(
                    question.getOptionB()
            );

            dto.setOptionC(
                    question.getOptionC()
            );

            dto.setOptionD(
                    question.getOptionD()
            );

            dto.setSelectedLetter(
                    answer.getSelectedAnswerLetter()
            );

            dto.setSelectedText(
                    answer.getSelectedAnswerText()
            );

            dto.setCorrectLetter(
                    question.getCorrectAnswerLetter()
            );

            dto.setCorrectText(
                    question.getCorrectAnswerText()
            );

            dto.setCorrect(
                    answer.isCorrect()
            );

            questionDtos.add(dto);

        }

        response.setQuestions(
                questionDtos
        );

        return response;

    }

}
