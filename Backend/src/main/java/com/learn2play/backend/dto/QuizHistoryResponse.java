package com.learn2play.backend.dto;

import java.time.LocalDateTime;

public class QuizHistoryResponse {

    private String attemptId;
    private String quizId;
    private String quizTitle;
    private String difficulty;
    private int totalQuestions;
    private int correctCount;
    private double scorePercent;
    private LocalDateTime attemptedAt;

    // TIMED QUIZ MODE (ANALYTICS)
    private boolean timedMode;
    private Integer timeTakenSeconds;
    private Integer remainingSeconds;

    public QuizHistoryResponse() {
    }

    public QuizHistoryResponse(
            String attemptId,
            String quizId,
            String quizTitle,
            int totalQuestions,
            int correctCount,
            double scorePercent,
            LocalDateTime attemptedAt) {
        this(attemptId, quizId, quizTitle, null, totalQuestions, correctCount, scorePercent, attemptedAt, false, null, null);
    }

    public QuizHistoryResponse(
            String attemptId,
            String quizId,
            String quizTitle,
            int totalQuestions,
            int correctCount,
            double scorePercent,
            LocalDateTime attemptedAt,
            boolean timedMode,
            Integer timeTakenSeconds,
            Integer remainingSeconds) {
        this(attemptId, quizId, quizTitle, null, totalQuestions, correctCount, scorePercent, attemptedAt,
                timedMode, timeTakenSeconds, remainingSeconds);
    }

    public QuizHistoryResponse(
            String attemptId,
            String quizId,
            String quizTitle,
            String difficulty,
            int totalQuestions,
            int correctCount,
            double scorePercent,
            LocalDateTime attemptedAt,
            boolean timedMode,
            Integer timeTakenSeconds,
            Integer remainingSeconds) {

        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.difficulty = difficulty;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.scorePercent = scorePercent;
        this.attemptedAt = attemptedAt;
        this.timedMode = timedMode;
        this.timeTakenSeconds = timeTakenSeconds;
        this.remainingSeconds = remainingSeconds;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
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