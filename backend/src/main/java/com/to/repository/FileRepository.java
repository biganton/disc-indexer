package com.to.repository;

import com.to.model.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FileRepository extends MongoRepository<FileDocument, String> {

    Optional<FileDocument> findByFilePath(String filePath);
}
