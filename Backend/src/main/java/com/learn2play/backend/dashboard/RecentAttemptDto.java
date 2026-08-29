package com.learn2play.backend.dashboard;

public class RecentAttemptDto {

    private String title;

    private double score;

    private String date;

    public RecentAttemptDto() {
    }

    public RecentAttemptDto(
            String title,
            double score,
            String date
    ) {

        this.title = title;

        this.score = score;

        this.date = date;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}