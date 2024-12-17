package com.to.service;

import com.to.logic.ZipArchiver;
import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public void moveFile(String filePath, String destinationPath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path is required");
        }
        if (destinationPath == null || destinationPath.isEmpty()) {
            throw new IllegalArgumentException("Destination path is required");
        }

        File file = new File(filePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        Path path = Paths.get(filePath);
        Path fileName = path.getFileName();

        try {
            Files.move(path, Path.of(destinationPath).resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                fileDocument.setFilePath(targetDirectoryPath);
                fileRepository.save(fileDocument);
            } else {
                throw new IOException("Failed to move file: " + file.getAbsolutePath());
            }
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
