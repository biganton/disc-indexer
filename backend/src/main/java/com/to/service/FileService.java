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

    public void processDirectory(String directoryPath) throws IOException, NoSuchAlgorithmException {
        fileManagementService.deleteAllFiles();
        File directory = new File(directoryPath);
        fileProcessingService.processDirectory(directory);
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

    public void moveDuplicatesToGroupedDirectories(String targetDirectoryPath) throws IOException {
        List<List<FileDocument>> duplicateGroups = fileAnalysisService.findDuplicates();
        fileManagementService.moveDuplicatesToGroupedDirectories(duplicateGroups, targetDirectoryPath);
    }

    public void moveVersionsToGroupedDirectories(String targetDirectoryPath, int threshold) throws IOException {
        List<List<FileDocument>> versionGroups = fileAnalysisService.findFileVersions(threshold);
        fileManagementService.moveVersionsToGroupedDirectories(versionGroups, targetDirectoryPath);
    }

    public void moveSelectedFilesToDirectory(List<String> fileIds, String targetDirectoryPath) throws IOException {
        fileManagementService.moveFilesToDirectory(fileIds, targetDirectoryPath);
    }

    public void archiveDirectory(String directoryPath, String targetDirectoryPath) {
        fileManagementService.archiveDirectory(directoryPath, targetDirectoryPath);
    }
}
