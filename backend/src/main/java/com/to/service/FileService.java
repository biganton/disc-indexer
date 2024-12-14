package com.to.service;

import com.to.model.FileDocument;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class FileService {

    private final FileProcessingService fileProcessingService;
    private final FileManagementService fileManagementService;
    private final FileAnalysisService fileAnalysisService;

    public FileService(FileProcessingService fileProcessingService, FileManagementService fileManagementService, FileAnalysisService fileAnalysisService) {
        this.fileProcessingService = fileProcessingService;
        this.fileManagementService = fileManagementService;
        this.fileAnalysisService = fileAnalysisService;
    }

    public void processFolder(String folderPath) throws IOException, NoSuchAlgorithmException {
        fileManagementService.deleteAllFiles();
        File folder = new File(folderPath);
        fileProcessingService.processFolder(folder);
    }

    public void deleteAllFiles() {
        fileManagementService.deleteAllFiles();
    }

    public List<FileDocument> getAllFiles() {
        return fileManagementService.getAllFiles();
    }

    public List<FileDocument> findLargestFiles(int limit) {
        return fileAnalysisService.findLargestFiles(limit);
    }

    public List<List<FileDocument>> findDuplicates() {
        return fileAnalysisService.findDuplicates();
    }

    public List<List<FileDocument>> findFileVersions(int threshold) {
        return fileAnalysisService.findFileVersions(threshold);
    }

    public void openFile(String filePath) throws IOException {
        fileManagementService.openFile(filePath);
    }

    public void deleteFile(String fileId) throws IOException {
        fileManagementService.deleteFile(fileId);
    }
}
