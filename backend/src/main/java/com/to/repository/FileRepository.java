package com.to.repository;

import com.to.model.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<FileDocument, String> {

}
