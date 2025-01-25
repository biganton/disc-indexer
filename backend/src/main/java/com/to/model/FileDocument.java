package com.to.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "files")
public class FileDocument {
    @Id
    private String id;
    private String fileName;
    private String filePath;
    private long size;
    private String hash;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    @TextIndexed
    private String content;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
