package com.to.repository;

import com.to.model.ActionLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActionLogRepository extends MongoRepository<ActionLog, String> {
    List<ActionLog> findByArchived(boolean archived);
    List<ActionLog> findByFilePath(String filePath);
    List<ActionLog> findByStatus(String status);
}
