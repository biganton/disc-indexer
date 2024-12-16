package com.to.service;

import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
}
