package com.learn2play.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "quizzes")
public class Quiz {

    @Id
    private String id;

    // Reference by id instead of embedding/joining the UploadedDocument —
    // Mongo has no foreign keys, so we just store the related document's id.
    private String uploadedDocumentId;

    private String title;

    // "easy", "intermediate", or "hard" — the difficulty level the quiz
    // was generated at. Defaults to "intermediate" for older quizzes that
    // predate this field.
    private String difficulty = "intermediate";

    private LocalDateTime createdAt = LocalDateTime.now();

    // Questions are embedded directly in the Quiz document (was @OneToMany
    // with cascade + orphanRemoval — embedding gives the same "questions
    // live and die with their quiz" behavior naturally in Mongo).
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
    }

    public Quiz(String uploadedDocumentId, String title) {
        this.uploadedDocumentId = uploadedDocumentId;
        this.title = title;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUploadedDocumentId() {
        return uploadedDocumentId;
    }

    public void setUploadedDocumentId(String uploadedDocumentId) {
        this.uploadedDocumentId = uploadedDocumentId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
