package com.to.service;

import com.to.logic.ActionStatus;
import com.to.logic.ActionType;
import com.to.model.ActionLog;
import com.to.model.FileDocument;
import com.to.repository.ActionLogRepository;
import com.to.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ActionLogService {
    private final ActionLogRepository actionLogRepository;
    private final FileRepository fileRepository;
    private final FileProcessingService fileProcessingService;

    public ActionLogService(ActionLogRepository actionLogRepository, FileRepository fileRepository, FileProcessingService fileProcessingService) {
        this.actionLogRepository = actionLogRepository;
        this.fileRepository = fileRepository;
        this.fileProcessingService = fileProcessingService;
    }

    public List<ActionLog> getAllActionLogs() {
        return actionLogRepository.findAll();
    }

    public void deleteAllLogs(){
        actionLogRepository.deleteAll();
    }

    public void logOpenFile(String filePath, boolean isSuccessful) {
        ActionLog actionLog = new ActionLog();
        actionLog.setActionType(String.valueOf(ActionType.OPEN_FILE));
        actionLog.setFilePath(filePath);
        actionLog.setTimestamp(LocalDateTime.now());
        actionLog.setStatus(isSuccessful ? ActionStatus.SUCCESS.toString(): ActionStatus.FAILURE.toString());
        actionLog.setArchived(false);
        actionLogRepository.save(actionLog);
    }

    public void changeLogStatus(String logId, ActionStatus status) {
        ActionLog actionLog = actionLogRepository.findById(logId).orElseThrow(() ->
                new IllegalArgumentException("Action log not found: " + logId));
        actionLog.setStatus(status.toString());
        actionLogRepository.save(actionLog);
    }

    public String logDeleteFile(String fileId) throws IOException {
        FileDocument fileDocument = fileRepository.findById(fileId).orElseThrow(() ->
                new IllegalArgumentException("File not found: " + fileId));

        byte[] fileBytes = Files.readAllBytes(Path.of(fileDocument.getFilePath()));
        String encodedContent = Base64.getEncoder().encodeToString(fileBytes);

        ActionLog actionLog = new ActionLog();
        actionLog.setActionType(String.valueOf(ActionType.DELETE_FILE));
        actionLog.setFilePath(fileDocument.getFilePath());
        actionLog.setFileContentBase64(encodedContent);
        actionLog.setTimestamp(LocalDateTime.now());
        actionLog.setStatus(ActionStatus.PENDING.toString());
        actionLog.setArchived(false);

        actionLogRepository.save(actionLog);
        return actionLog.getId();
    }

    public String logMoveFiles(String sourceDirectoryPath, String targetDirectoryPath, boolean isSuccessful, boolean isArchived) {
        ActionLog actionLog = new ActionLog();
        actionLog.setActionType(String.valueOf(ActionType.MOVE_FILES));
        actionLog.setFilePath(sourceDirectoryPath);
        actionLog.setTargetPath(targetDirectoryPath);
        actionLog.setTimestamp(LocalDateTime.now());
        actionLog.setStatus(isSuccessful ? ActionStatus.SUCCESS.toString(): ActionStatus.FAILURE.toString());
        actionLog.setArchived(isArchived);
        actionLogRepository.save(actionLog);
        return actionLog.getId();
    }

    public String logArchiveFiles(String sourceDirectoryPath, String targetDirectoryPath, boolean isSuccessful, boolean isArchived) {
        ActionLog actionLog = new ActionLog();
        actionLog.setActionType(String.valueOf(ActionType.ARCHIVE_FILES));
        actionLog.setFilePath(sourceDirectoryPath);
        actionLog.setTargetPath(targetDirectoryPath);
        actionLog.setTimestamp(LocalDateTime.now());
        actionLog.setStatus(isSuccessful ? ActionStatus.SUCCESS.toString(): ActionStatus.FAILURE.toString());
        actionLog.setArchived(isArchived);
        actionLogRepository.save(actionLog);
        return actionLog.getId();
    }

    public void revertAction(String actionLogId) throws IOException, NoSuchAlgorithmException {
        ActionLog retrievedLog = actionLogRepository.findById(actionLogId).orElseThrow(() ->
                new IllegalArgumentException("Action log not found: " + actionLogId));
        switch (retrievedLog.getActionType()) {
            case "DELETE_FILE":
                byte[] decodedContent = Base64.getDecoder().decode(retrievedLog.getFileContentBase64());
                Files.write(Path.of(retrievedLog.getFilePath()), decodedContent);
                fileProcessingService.processFile(retrievedLog.getFilePath());
                changeLogStatus(actionLogId, ActionStatus.REVERTED);
                break;
            case "MOVE_FILES":
                File destinationPath = new File(retrievedLog.getTargetPath());
                String sourcePath = retrievedLog.getFilePath();
                if (destinationPath.exists() && !new File(sourcePath).exists()) {
                    Files.copy(Path.of(retrievedLog.getTargetPath()), Path.of(sourcePath));
                    Files.delete(Path.of(retrievedLog.getTargetPath()));
                    changeLogStatus(actionLogId, ActionStatus.REVERTED);
                    Optional<FileDocument> file = fileRepository.findByFilePath(retrievedLog.getTargetPath());
                    if (file.isPresent()) {
                        FileDocument existingFile = file.get();
                        existingFile.setFilePath(sourcePath);
                        existingFile.setLastModified(LocalDateTime.now());
                        fileRepository.save(existingFile);
                    }

                    File parentFolder = destinationPath.getParentFile();
                    if (parentFolder != null && parentFolder.isDirectory() && parentFolder.listFiles() != null) {
                        if (parentFolder.listFiles().length == 0) {
                            parentFolder.delete();
                        }
                    }

                } else {
                    throw new IOException("Failed to revert action: " + retrievedLog.getActionType());
                }
                break;
            case "ARCHIVE_FILES":
                String zipFilePath = retrievedLog.getTargetPath();
                String targetDirectoryPath = retrievedLog.getFilePath();

                File zipFile = new File(zipFilePath);
                if (!zipFile.exists()) {
                    throw new IOException("ZIP file not found at: " + zipFilePath);
                }

                File targetDirectory = new File(targetDirectoryPath);
                if (!targetDirectory.exists()) {
                    if (!targetDirectory.mkdirs()) {
                        throw new IOException("Failed to create target directory: " + targetDirectoryPath);
                    }
                }

                try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(zipFile))) {
                    java.util.zip.ZipEntry zipEntry;
                    while ((zipEntry = zis.getNextEntry()) != null) {
                        File extractedFile = new File(targetDirectory, zipEntry.getName());
                        if (zipEntry.isDirectory()) {
                            if (!extractedFile.mkdirs() && !extractedFile.isDirectory()) {
                                throw new IOException("Failed to create directory: " + extractedFile.getAbsolutePath());
                            }
                        } else {
                            try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, length);
                                }
                            }
                        }
                        zis.closeEntry();
                    }
                } catch (IOException e) {
                    throw new IOException("Failed to extract ZIP file: " + zipFilePath, e);
                }

                if (!zipFile.delete()) {
                    throw new IOException("Failed to delete ZIP file: " + zipFilePath);
                }

                File[] extractedFiles = targetDirectory.listFiles();
                if (extractedFiles != null && extractedFiles.length == 1 && extractedFiles[0].isDirectory()) {
                    File innerFolder = extractedFiles[0];
                    File[] innerFiles = innerFolder.listFiles();
                    if (innerFiles != null) {
                        for (File file : innerFiles) {
                            File newLocation = new File(targetDirectory, file.getName());
                            if (!file.renameTo(newLocation)) {
                                throw new IOException("Failed to move file: " + file.getAbsolutePath());
                            }
                        }
                    }

                    if (!innerFolder.delete()) {
                        throw new IOException("Failed to delete empty folder: " + innerFolder.getAbsolutePath());
                    }
                }
                changeLogStatus(actionLogId, ActionStatus.REVERTED);
                break;
            default:
                throw new IllegalArgumentException("Unsupported action type: " + retrievedLog.getActionType());
        }
    }

}
