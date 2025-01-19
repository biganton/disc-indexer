package com.to.service;

import com.to.logic.ActionType;
import com.to.model.ActionLog;
import com.to.model.FileDocument;
import com.to.repository.ActionLogRepository;
import com.to.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class ActionLogService {
    private final ActionLogRepository actionLogRepository;
    private final FileRepository fileRepository;

    public ActionLogService(ActionLogRepository actionLogRepository, FileRepository fileRepository) {
        this.actionLogRepository = actionLogRepository;
        this.fileRepository = fileRepository;
    }

    public void logOpenFile(String filePath) {
        ActionLog actionLog = new ActionLog();
        actionLog.setActionType(String.valueOf(ActionType.OPEN_FILE));
        actionLog.setFilePath(filePath);
        actionLog.setTimestamp(LocalDateTime.now());
        actionLog.setStatus("PENDING");
        actionLog.setArchived(false);
        actionLogRepository.save(actionLog);
    }

    public void logDeleteFile(String fileId) throws IOException {
        FileDocument fileDocument = fileRepository.findById(fileId).orElseThrow(() ->
                new IllegalArgumentException("File not found: " + fileId));

        byte[] fileBytes = Files.readAllBytes(Path.of(fileDocument.getFilePath()));
        String encodedContent = Base64.getEncoder().encodeToString(fileBytes);

        ActionLog actionLog = new ActionLog();
        actionLog.setActionType(String.valueOf(ActionType.DELETE_FILE));
        actionLog.setFilePath(fileDocument.getFilePath());
        actionLog.setFileContentBase64(encodedContent);
        actionLog.setTimestamp(LocalDateTime.now());
        actionLog.setStatus("PENDING");
        actionLog.setArchived(false);

        actionLogRepository.save(actionLog);
    }

}
