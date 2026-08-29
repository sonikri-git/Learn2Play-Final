package com.learn2play.backend.dashboard;

import java.util.List;

public class DashboardResponse {

    private String name;

    private String email;

    private long totalQuizzes;

    private long uploadedDocuments;

    private double averageScore;

    private double highestScore;

    private double lowestScore;

    private double accuracy;

    private long totalQuestionsAnswered;

    private long totalCorrectAnswers;

    private long totalWrongAnswers;

    private List<Double> scores;

    private List<String> dates;

    private List<RecentAttemptDto> recentAttempts;

    private List<AchievementDto> achievements;

    public DashboardResponse() {
    }

    public DashboardResponse(

            String name,

            String email,

            long totalQuizzes,

            long uploadedDocuments,

            double averageScore,

            double highestScore,

            double lowestScore,

            double accuracy,

            long totalQuestionsAnswered,

            long totalCorrectAnswers,

            long totalWrongAnswers,

            List<Double> scores,

            List<String> dates,

            List<RecentAttemptDto> recentAttempts,

            List<AchievementDto> achievements

    ) {

        this.name = name;

        this.email = email;

        this.totalQuizzes = totalQuizzes;

        this.uploadedDocuments = uploadedDocuments;

        this.averageScore = averageScore;

        this.highestScore = highestScore;

        this.lowestScore = lowestScore;

        this.accuracy = accuracy;

        this.totalQuestionsAnswered = totalQuestionsAnswered;

        this.totalCorrectAnswers = totalCorrectAnswers;

        this.totalWrongAnswers = totalWrongAnswers;

        this.scores = scores;

        this.dates = dates;

        this.recentAttempts = recentAttempts;

        this.achievements = achievements;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(long totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public long getUploadedDocuments() {
        return uploadedDocuments;
    }

    public void setUploadedDocuments(long uploadedDocuments) {
        this.uploadedDocuments = uploadedDocuments;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(double highestScore) {
        this.highestScore = highestScore;
    }

    public double getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(double lowestScore) {
        this.lowestScore = lowestScore;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public void setTotalQuestionsAnswered(long totalQuestionsAnswered) {
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }

    public long getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(long totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public long getTotalWrongAnswers() {
        return totalWrongAnswers;
    }

    public void setTotalWrongAnswers(long totalWrongAnswers) {
        this.totalWrongAnswers = totalWrongAnswers;
    }

    public List<Double> getScores() {
        return scores;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<RecentAttemptDto> getRecentAttempts() {
        return recentAttempts;
    }

    public void setRecentAttempts(List<RecentAttemptDto> recentAttempts) {
        this.recentAttempts = recentAttempts;
    }

    public List<AchievementDto> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<AchievementDto> achievements) {
        this.achievements = achievements;
    }

}