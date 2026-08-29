package com.learn2play.backend.repository;

import com.learn2play.backend.entity.UploadedDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UploadedDocumentRepository extends MongoRepository<UploadedDocument, String> {

    Optional<UploadedDocument> findByFileId(String fileId);

    Optional<UploadedDocument> findTopByOrderByUploadedAtDesc();

    long countByUserEmail(String userEmail);

    List<UploadedDocument> findByUserEmailOrderByUploadedAtDesc(String userEmail);
}
