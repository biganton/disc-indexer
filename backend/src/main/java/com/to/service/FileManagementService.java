package com.to.service;

import com.to.logic.ZipArchiver;
import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class FileManagementService {
    private final FileRepository fileRepository;

    public FileManagementService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void deleteAllFiles() {
        fileRepository.deleteAll();
    }

    public void openFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path is required");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", file.getAbsolutePath()});
        } else if (os.contains("mac")) {
            Runtime.getRuntime().exec(new String[]{"open", file.getAbsolutePath()});
        } else if (os.contains("nix") || os.contains("nux")) {
            Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
        } else {
            throw new UnsupportedOperationException("Unsupported operating system");
        }
    }

    public void deleteFile(String fileId) throws IOException {
        FileDocument fileDocument = fileRepository.findById(fileId).orElseThrow(() ->
                new IllegalArgumentException("File not found: " + fileId));
        File file = new File(fileDocument.getFilePath());

        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to delete file from system");
        }

        fileRepository.deleteById(fileId);
    }

    public List<FileDocument> getAllFiles() {
        return fileRepository.findAll();
    }

    public void moveFilesToDirectory(String targetDirectoryPath, List<FileDocument> files) throws IOException {
        File targetDirectory = new File(targetDirectoryPath);
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IOException("Failed to create directory: " + targetDirectoryPath);
        }

        for (FileDocument fileDocument : files) {
            File file = new File(fileDocument.getFilePath());
            File newFile = new File(targetDirectoryPath, file.getName());
            if (file.renameTo(newFile)) {
                fileDocument.setFilePath(newFile.getAbsolutePath());
                fileRepository.save(fileDocument);
            } else {
                throw new IOException("Failed to move file: " + file.getAbsolutePath());
            }
        }
    }

    public void moveFilesToDirectory(List<String> fileIds, String targetDirectoryPath) throws IOException {
        List<FileDocument> selectedFiles = fileRepository.findAllById(fileIds);

        if (selectedFiles.isEmpty()) {
            throw new IllegalArgumentException("No files found with the provided IDs.");
        }
        moveFilesToDirectory(targetDirectoryPath, selectedFiles);
    }

    public void moveDuplicatesToGroupedDirectories( List<List<FileDocument>> duplicateGroups, String targetDirectoryPath) throws IOException {
        for (int i = 0; i < duplicateGroups.size(); i++) {
            List<FileDocument> group = duplicateGroups.get(i);
            String groupDirectoryPath = targetDirectoryPath + "/duplicates" + (i + 1);
            moveFilesToDirectory(groupDirectoryPath, group);
        }
    }
    public void moveVersionsToGroupedDirectories( List<List<FileDocument>> versionGroups, String targetDirectoryPath) throws IOException {
        for (int i = 0; i < versionGroups.size(); i++) {
            List<FileDocument> group = versionGroups.get(i);
            String groupDirectoryPath = targetDirectoryPath + "/versions" + (i + 1);
            moveFilesToDirectory(groupDirectoryPath, group);
        }
    }

    public void archiveDirectory(String directoryPath, String targetDirectoryPath) {
        ZipArchiver zipArchiver = new ZipArchiver();
        try {
            zipArchiver.zipFolder(directoryPath, targetDirectoryPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
