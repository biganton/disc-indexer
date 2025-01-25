package com.to.repository;

import com.to.model.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends MongoRepository<FileDocument, String> {

    Optional<FileDocument> findByFilePath(String filePath);

    @Query("{ $text: { $search: ?0 } }")
    List<FileDocument> searchByKeyword(String keyword);
}
