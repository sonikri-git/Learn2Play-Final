package com.learn2play.backend.dto;

import java.util.List;

public class ReviewResponse {

    private String attemptId;

    private String quizId;

    private String quizTitle;

    private String userEmail;

    private int totalQuestions;

    private int correctAnswers;

    private double scorePercent;

    private List<ReviewQuestionDto> questions;

    public ReviewResponse() {
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

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public double getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(double scorePercent) {
        this.scorePercent = scorePercent;
    }

    public List<ReviewQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ReviewQuestionDto> questions) {
        this.questions = questions;
    }
}