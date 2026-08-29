package com.learn2play.backend.dto;

public class ProfileResponse {

    private String name;
    private String email;
    private long totalQuizzes;
    private double averageScore;
    private double highestScore;
    private long uploadedDocuments;

    public ProfileResponse() {
    }

    public ProfileResponse(
            String name,
            String email,
            long totalQuizzes,
            double averageScore,
            double highestScore,
            long uploadedDocuments
    ) {
        this.name = name;
        this.email = email;
        this.totalQuizzes = totalQuizzes;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.uploadedDocuments = uploadedDocuments;
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

    public long getUploadedDocuments() {
        return uploadedDocuments;
    }

    public void setUploadedDocuments(long uploadedDocuments) {
        this.uploadedDocuments = uploadedDocuments;
    }

}