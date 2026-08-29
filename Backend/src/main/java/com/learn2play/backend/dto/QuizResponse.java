package com.learn2play.backend.dto;

import com.learn2play.backend.QuizItem;

import java.util.ArrayList;
import java.util.List;

public class QuizResponse {
    private String quizId;
    private String title;
    private String difficulty;
    private List<QuizItem> questions = new ArrayList<>();

    public QuizResponse() {
    }

    // Kept for backward compatibility with any existing callers.
    public QuizResponse(String quizId, String title, List<QuizItem> questions) {
        this(quizId, title, "intermediate", questions);
    }

    public QuizResponse(String quizId, String title, String difficulty, List<QuizItem> questions) {
        this.quizId = quizId;
        this.title = title;
        this.difficulty = difficulty;
        this.questions = questions;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<QuizItem> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizItem> questions) {
        this.questions = questions;
    }
}
