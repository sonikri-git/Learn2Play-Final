package com.learn2play.backend.repository;

import com.learn2play.backend.entity.Certificate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CertificateRepository extends MongoRepository<Certificate, String> {
    Optional<Certificate> findByCertificateCode(String certificateCode);
}
