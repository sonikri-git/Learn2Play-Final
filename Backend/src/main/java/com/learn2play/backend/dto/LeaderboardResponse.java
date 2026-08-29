package com.learn2play.backend.dto;

public class LeaderboardResponse {

    private int rank;
    private String name;
    private String email;
    private double averageScore;
    private double highestScore;
    private int quizzesTaken;

    public LeaderboardResponse() {
    }

    public LeaderboardResponse(int rank,
                               String name,
                               String email,
                               double averageScore,
                               double highestScore,
                               int quizzesTaken) {

        this.rank = rank;
        this.name = name;
        this.email = email;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.quizzesTaken = quizzesTaken;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public int getQuizzesTaken() {
        return quizzesTaken;
    }

    public void setQuizzesTaken(int quizzesTaken) {
        this.quizzesTaken = quizzesTaken;
    }
}