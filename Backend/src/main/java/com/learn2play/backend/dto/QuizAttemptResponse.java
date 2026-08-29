package com.learn2play.backend.dto;

public class QuizAttemptResponse {
    private String attemptId;
    private String quizId;
    private int totalQuestions;
    private int correctCount;
    private double scorePercent;
    private String message;

    // TIMED QUIZ MODE (ANALYTICS) - echoed back so the frontend can
    // confirm what was actually persisted for this attempt.
    private boolean timedMode;
    private Integer timeTakenSeconds;
    private Integer remainingSeconds;

    public QuizAttemptResponse() {
    }

    public QuizAttemptResponse(String attemptId, String quizId, int totalQuestions, int correctCount, double scorePercent, String message) {
        this(attemptId, quizId, totalQuestions, correctCount, scorePercent, message, false, null, null);
    }

    public QuizAttemptResponse(String attemptId, String quizId, int totalQuestions, int correctCount, double scorePercent,
                                String message, boolean timedMode, Integer timeTakenSeconds, Integer remainingSeconds) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.scorePercent = scorePercent;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
