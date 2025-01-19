package com.to.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "action_logs")
public class ActionLog {
    @Id
    private String id;
    private String actionType;
    private String filePath;
    private String targetPath;
    private boolean archived;
    private String fileContentBase64;
    private LocalDateTime timestamp;
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public String getFileContentBase64() { return fileContentBase64; }
    public void setFileContentBase64(String fileContentBase64) { this.fileContentBase64 = fileContentBase64; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
