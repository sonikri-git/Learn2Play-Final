package com.learn2play.backend.dto;

import java.util.List;

public class StatisticsResponse {

	private String name;
	private String email;

	private long totalQuizzes;
	private long uploadedDocuments;

	private double averageScore;
	private double highestScore;
	private double lowestScore;

	private long totalQuestionsAnswered;
	private long totalCorrectAnswers;
	private long totalWrongAnswers;

	private double accuracy;

	private List<Double> scores;
	private List<String> dates;

    public StatisticsResponse() {
    }

    public StatisticsResponse(
    		  String name,
    		    String email,
    		    long totalQuizzes,
    		    long uploadedDocuments,
    		    double averageScore,
    		    double highestScore,
    		    double lowestScore,
    		    long totalQuestionsAnswered,
    		    long totalCorrectAnswers,
    		    long totalWrongAnswers,
    		    double accuracy,
    		    List<Double> scores,
    		    List<String> dates
    ) {

        this.name = name;
        this.email = email;
        this.totalQuizzes = totalQuizzes;
        this.uploadedDocuments = uploadedDocuments;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.totalQuestionsAnswered = totalQuestionsAnswered;
        this.accuracy = accuracy;
        this.scores = scores;
        this.dates = dates;
        this.totalCorrectAnswers = totalCorrectAnswers;
        this.totalWrongAnswers = totalWrongAnswers;

    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public long getTotalQuizzes() { return totalQuizzes; }

    public void setTotalQuizzes(long totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public long getUploadedDocuments() { return uploadedDocuments; }

    public void setUploadedDocuments(long uploadedDocuments) { this.uploadedDocuments = uploadedDocuments; }

    public double getAverageScore() { return averageScore; }

    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getHighestScore() { return highestScore; }

    public void setHighestScore(double highestScore) { this.highestScore = highestScore; }

    public double getLowestScore() { return lowestScore; }

    public void setLowestScore(double lowestScore) { this.lowestScore = lowestScore; }

    public long getTotalQuestionsAnswered() { return totalQuestionsAnswered; }

    public void setTotalQuestionsAnswered(long totalQuestionsAnswered) { this.totalQuestionsAnswered = totalQuestionsAnswered; }

    public double getAccuracy() { return accuracy; }

    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public List<Double> getScores() { return scores; }

    public void setScores(List<Double> scores) { this.scores = scores; }

    public List<String> getDates() { return dates; }

    public void setDates(List<String> dates) { this.dates = dates; }

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

}