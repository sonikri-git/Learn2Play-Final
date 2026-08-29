package com.learn2play.backend.entity;

import java.util.UUID;

/**
 * Embedded inside Quiz.questions — MongoDB has no JOINs, so a Question only
 * exists as part of its parent Quiz document. It keeps its own id (a UUID
 * string, generated here since Mongo does not auto-assign ids to
 * sub-documents) so it can still be referenced from UserAnswer.questionId.
 */
public class Question {

    private String id = UUID.randomUUID().toString();

    private String type;
    private String questionText;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctAnswerLetter;
    private String correctAnswerText;

    private int sortOrder;

    public Question() {
    }

    public Question(String type, String questionText, int sortOrder) {
        this.type = type;
        this.questionText = questionText;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswerLetter() {
        return correctAnswerLetter;
    }

    public void setCorrectAnswerLetter(String correctAnswerLetter) {
        this.correctAnswerLetter = correctAnswerLetter;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
