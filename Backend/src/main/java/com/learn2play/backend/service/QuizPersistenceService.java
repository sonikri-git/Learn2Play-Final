package com.learn2play.backend.service;

import com.learn2play.backend.QuizItem;
import com.learn2play.backend.dto.AnswerSubmission;
import com.learn2play.backend.dto.QuizAttemptRequest;
import com.learn2play.backend.dto.QuizAttemptResponse;
import com.learn2play.backend.dto.QuizResponse;
import com.learn2play.backend.entity.Question;
import com.learn2play.backend.entity.Quiz;
import com.learn2play.backend.entity.QuizAttempt;
import com.learn2play.backend.entity.UploadedDocument;
import com.learn2play.backend.entity.UserAnswer;
import com.learn2play.backend.repository.QuizAttemptRepository;
import com.learn2play.backend.repository.QuizRepository;
import org.springframework.stereotype.Service;
import com.learn2play.backend.dto.QuizHistoryResponse;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizPersistenceService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public QuizPersistenceService(QuizRepository quizRepository,
                                  QuizAttemptRepository quizAttemptRepository) {
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }


    public List<QuizHistoryResponse> getQuizHistory(String email) {

        return quizAttemptRepository
                .findByUserEmailOrderByAttemptedAtDesc(email)
                .stream()
                .map(attempt -> {
                    // Quiz is no longer embedded on QuizAttempt (it's referenced
                    // by id), so we look it up to get its title and difficulty.
                    Quiz relatedQuiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
                    String quizTitle = relatedQuiz != null ? relatedQuiz.getTitle() : "Unknown Quiz";
                    String difficulty = relatedQuiz != null ? relatedQuiz.getDifficulty() : null;

                    return new QuizHistoryResponse(
                            attempt.getId(),
                            attempt.getQuizId(),
                            quizTitle,
                            difficulty,
                            attempt.getTotalQuestions(),
                            attempt.getCorrectCount(),
                            attempt.getScorePercent(),
                            attempt.getAttemptedAt(),
                            attempt.isTimedMode(),
                            attempt.getTimeTakenSeconds(),
                            attempt.getRemainingSeconds()
                    );
                })
                .collect(Collectors.toList());

    }

    public Quiz saveGeneratedQuiz(UploadedDocument document, List<QuizItem> generatedItems) {
        return saveGeneratedQuiz(document, generatedItems, "intermediate");
    }

    public Quiz saveGeneratedQuiz(UploadedDocument document, List<QuizItem> generatedItems, String difficulty) {
        String title = "Quiz for " + document.getOriginalFileName();
        Quiz quiz = new Quiz(document.getId(), title);
        quiz.setDifficulty(
                (difficulty == null || difficulty.isBlank()) ? "intermediate" : difficulty.toLowerCase()
        );

        int order = 1;
        for (QuizItem item : generatedItems) {
            Question question = new Question(
                    item.getType() == null ? "Unknown" : item.getType(),
                    item.getQuestion(),
                    order++
            );

            Map<String, String> options = item.getOptions();
            if (options != null) {
                question.setOptionA(options.get("A"));
                question.setOptionB(options.get("B"));
                question.setOptionC(options.get("C"));
                question.setOptionD(options.get("D"));
            }

            question.setCorrectAnswerLetter(item.getAnswerLetter());
            question.setCorrectAnswerText(item.getAnswerText() != null ? item.getAnswerText() : item.getAnswer());
            quiz.addQuestion(question);
        }

        return quizRepository.save(quiz);
    }

    public QuizResponse getLatestQuizResponse() {
        Quiz quiz = quizRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("No quiz found yet"));
        List<QuizItem> items = toSortedQuizItems(quiz);
        return new QuizResponse(quiz.getId(), quiz.getTitle(), quiz.getDifficulty(), items);
    }

    public List<QuizItem> getLatestQuizItems() {
        return getLatestQuizResponse().getQuestions();
    }

    public List<QuizItem> getQuizItems(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
        return toSortedQuizItems(quiz);
    }

    public QuizAttemptResponse submitAttempt(String quizId, QuizAttemptRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));

        Map<String, Question> questionsById = quiz.getQuestions()
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setUserEmail(request.getUserEmail() == null || request.getUserEmail().isBlank()
                ? "guest@learn2play.local"
                : request.getUserEmail());

        int correctCount = 0;

        for (AnswerSubmission submitted : request.getAnswers()) {
            Question question = questionsById.get(submitted.getQuestionId());
            if (question == null) {
                continue;
            }

            boolean correct = isCorrect(question, submitted);
            if (correct) {
                correctCount++;
            }

            UserAnswer answer = new UserAnswer();
            answer.setQuestionId(question.getId());
            answer.setSelectedAnswerLetter(normalize(submitted.getSelectedAnswerLetter()));
            answer.setSelectedAnswerText(submitted.getSelectedAnswerText());
            answer.setCorrect(correct);
            attempt.addAnswer(answer);
        }

        int total = questionsById.size();
        double scorePercent = total == 0 ? 0 : (correctCount * 100.0) / total;

        attempt.setTotalQuestions(total);
        attempt.setCorrectCount(correctCount);
        attempt.setScorePercent(scorePercent);

        // TIMED QUIZ MODE (ANALYTICS)
        // timedMode defaults to false when the frontend omits it (older
        // clients / Practice Mode attempts that never set the flag).
        boolean timedMode = Boolean.TRUE.equals(request.getTimedMode());
        attempt.setTimedMode(timedMode);
        attempt.setTimeTakenSeconds(request.getTimeTakenSeconds());
        // remainingSeconds is only meaningful in Exam Mode; ignore it
        // otherwise so it doesn't leak stray values into history.
        attempt.setRemainingSeconds(timedMode ? request.getRemainingSeconds() : null);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        return new QuizAttemptResponse(
                savedAttempt.getId(),
                quizId,
                total,
                correctCount,
                Math.round(scorePercent * 100.0) / 100.0,
                "Quiz attempt saved successfully",
                savedAttempt.isTimedMode(),
                savedAttempt.getTimeTakenSeconds(),
                savedAttempt.getRemainingSeconds()
        );
    }

    private List<QuizItem> toSortedQuizItems(Quiz quiz) {
        return quiz.getQuestions()
                .stream()
                .sorted(Comparator.comparingInt(Question::getSortOrder))
                .map(this::toQuizItem)
                .collect(Collectors.toList());
    }

    private QuizItem toQuizItem(Question question) {
        QuizItem item = new QuizItem();
        item.setId(question.getId());
        item.setType(question.getType());
        item.setQuestion(question.getQuestionText());

        Map<String, String> options = new HashMap<>();
        putIfNotBlank(options, "A", question.getOptionA());
        putIfNotBlank(options, "B", question.getOptionB());
        putIfNotBlank(options, "C", question.getOptionC());
        putIfNotBlank(options, "D", question.getOptionD());
        item.setOptions(options.isEmpty() ? null : options);

        // Keeping correct answers in the response helps your current demo/result page.
        // Later, you can hide these fields until after the user submits the quiz.
        item.setAnswerLetter(question.getCorrectAnswerLetter());
        item.setAnswerText(question.getCorrectAnswerText());
        item.setAnswer(question.getCorrectAnswerText());
        return item;
    }

    private boolean isCorrect(Question question, AnswerSubmission submitted) {
        String selectedLetter = normalize(submitted.getSelectedAnswerLetter());
        String correctLetter = normalize(question.getCorrectAnswerLetter());

        if (correctLetter != null && selectedLetter != null) {
            return correctLetter.equals(selectedLetter);
        }

        String selectedText = normalizeText(submitted.getSelectedAnswerText());
        String correctText = normalizeText(question.getCorrectAnswerText());
        return correctText != null && correctText.equals(selectedText);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase();
    }

    private void putIfNotBlank(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }
    public QuizResponse getQuizById(String quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Quiz not found: " + quizId)
                );

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDifficulty(),
                toSortedQuizItems(quiz)
        );
    }
}
