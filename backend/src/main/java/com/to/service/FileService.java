package com.to.service;

import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void processFolder(String folderPath) throws IOException, NoSuchAlgorithmException {
        fileRepository.deleteAll();
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Given path is not a directory!");
        }

        processFilesInFolder(folder);
    }

    private void processFilesInFolder(File folder) throws IOException, NoSuchAlgorithmException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                processFilesInFolder(file);
            } else {
                FileDocument fileDocument = createFileDocument(file);
                fileRepository.save(fileDocument);
            }
        }
    }

    private FileDocument createFileDocument(File file) throws IOException, NoSuchAlgorithmException {
        FileDocument document = new FileDocument();
        document.setFileName(file.getName());
        document.setFilePath(file.getAbsolutePath());
        document.setSize(file.length());
        document.setHash(computeFileHash(file));

        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        document.setCreatedAt(convertInstantToLocalDateTime(attrs.creationTime().toInstant()));
        document.setLastModified(convertInstantToLocalDateTime(attrs.lastModifiedTime().toInstant()));

        return document;
    }

    private LocalDateTime convertInstantToLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private String computeFileHash(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
