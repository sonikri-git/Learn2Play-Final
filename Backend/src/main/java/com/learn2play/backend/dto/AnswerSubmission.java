package com.learn2play.backend.dto;

public class AnswerSubmission {
    private String questionId;
    private String selectedAnswerLetter;
    private String selectedAnswerText;

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
}
