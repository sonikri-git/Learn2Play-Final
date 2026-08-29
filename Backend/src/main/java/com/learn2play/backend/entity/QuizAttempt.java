package com.learn2play.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "quiz_attempts")
public class QuizAttempt {

    @Id
    private String id;

    // Reference by id instead of the JPA @ManyToOne Quiz object.
    private String quizId;

    private String userEmail;

    private int totalQuestions;
    private int correctCount;
    private double scorePercent;

    private LocalDateTime attemptedAt = LocalDateTime.now();

    // =====================================
    // TIMED QUIZ MODE (ANALYTICS)
    // =====================================

    // Whether the timer was enabled (Exam Mode) for this attempt.
    private boolean timedMode = false;

    // Total time the user spent on the quiz, in seconds. Populated for
    // both Practice and Exam mode attempts.
    private Integer timeTakenSeconds;

    // Time left on the clock when the attempt was submitted (Exam
    // Mode only). 0 indicates the timer expired and the quiz was
    // auto-submitted; null for Practice Mode attempts.
    private Integer remainingSeconds;

    // Answers are embedded directly (was @OneToMany, cascade + orphanRemoval).
    private List<UserAnswer> answers = new ArrayList<>();

    public QuizAttempt() {
    }

    public void addAnswer(UserAnswer answer) {
        this.answers.add(answer);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public double getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(double scorePercent) {
        this.scorePercent = scorePercent;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public List<UserAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<UserAnswer> answers) {
        this.answers = answers;
    }

    public boolean isTimedMode() {
        return timedMode;
    }

    public void setTimedMode(boolean timedMode) {
        this.timedMode = timedMode;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Integer timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
