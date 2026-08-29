package com.learn2play.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "certificates")
public class Certificate {

    @Id
    private String id;

    // Reference by id instead of the JPA @OneToOne QuizAttempt object.
    private String quizAttemptId;

    @Indexed(unique = true)
    private String certificateCode;

    private LocalDateTime issuedAt = LocalDateTime.now();

    public Certificate() {
    }

    public Certificate(String quizAttemptId, String certificateCode) {
        this.quizAttemptId = quizAttemptId;
        this.certificateCode = certificateCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuizAttemptId() {
        return quizAttemptId;
    }

    public void setQuizAttemptId(String quizAttemptId) {
        this.quizAttemptId = quizAttemptId;
    }

    public String getCertificateCode() {
        return certificateCode;
    }

    public void setCertificateCode(String certificateCode) {
        this.certificateCode = certificateCode;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
