package com.learn2play.backend;

public class UploadResponse {

    private String fileId;
    private String fileName;
    private String message;
    private String documentId;
    private String quizId;
    private Double relevanceAccuracy;

    public UploadResponse() {
    }

    public UploadResponse(String fileId, String fileName, String message) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.message = message;
    }

    public UploadResponse(String fileId, String fileName, String message, String documentId, String quizId) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.message = message;
        this.documentId = documentId;
        this.quizId = quizId;
    }

    public UploadResponse(String fileId, String fileName, String message, String documentId, String quizId, Double relevanceAccuracy) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.message = message;
        this.documentId = documentId;
        this.quizId = quizId;
        this.relevanceAccuracy = relevanceAccuracy;
    }

    public Double getRelevanceAccuracy() {
        return relevanceAccuracy;
    }

    public void setRelevanceAccuracy(Double relevanceAccuracy) {
        this.relevanceAccuracy = relevanceAccuracy;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }
}
