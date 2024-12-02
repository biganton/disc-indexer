package com.to.service;

import com.to.logic.EditDistanceCalculator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            if (file.isHidden()) {
                continue;
            }
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

    public void deleteAllFiles() {
        fileRepository.deleteAll();
    }

    public List<FileDocument> getAllFiles() {
        return fileRepository.findAll();
    }

    public List<List<FileDocument>> findDuplicates() {
        Map<String, List<FileDocument>> groupedByHash = fileRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(FileDocument::getHash));

        return groupedByHash.values().stream()
                .filter(group -> group.size() > 1)
                .collect(Collectors.toList());
    }

    public List<List<FileDocument>> findFileVersions(int threshold) {
        List<FileDocument> allFiles = fileRepository.findAll();
        List<List<FileDocument>> versions = new ArrayList<>();

        for (int i = 0; i < allFiles.size(); i++) {
            FileDocument file1 = allFiles.get(i);
            List<FileDocument> similarFiles = new ArrayList<>();
            similarFiles.add(file1);

            for (int j = i + 1; j < allFiles.size(); j++) {
                FileDocument file2 = allFiles.get(j);
                int distance = EditDistanceCalculator.calculate(file1.getFileName(), file2.getFileName());

                if (distance <= threshold) {
                    similarFiles.add(file2);
                }
            }

            if (similarFiles.size() > 1) {
                versions.add(similarFiles);
            }
        }

        return versions;
    }
}
