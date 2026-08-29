package com.learn2play.backend.entity;

import java.util.UUID;

/**
 * Embedded inside QuizAttempt.answers (was @ManyToOne to QuizAttempt +
 * @ManyToOne to Question). Since it now lives inside its parent
 * QuizAttempt document, the back-reference to QuizAttempt is dropped, and
 * the Question reference becomes a plain questionId string pointing at a
 * Question embedded in the relevant Quiz document.
 */
public class UserAnswer {

    private String id = UUID.randomUUID().toString();

    private String questionId;

    private String selectedAnswerLetter;
    private String selectedAnswerText;

    private boolean correct;

    public UserAnswer() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getSelectedAnswerLetter() {
        return selectedAnswerLetter;
    }

    public void setSelectedAnswerLetter(String selectedAnswerLetter) {
        this.selectedAnswerLetter = selectedAnswerLetter;
    }

    public String getSelectedAnswerText() {
        return selectedAnswerText;
    }

    public void setSelectedAnswerText(String selectedAnswerText) {
        this.selectedAnswerText = selectedAnswerText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
