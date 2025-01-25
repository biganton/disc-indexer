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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class FileProcessingService {
    private final FileRepository fileRepository;

    public FileProcessingService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void processDirectory(File directory) throws IOException, NoSuchAlgorithmException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Given path is not a directory!");
        }

        for (File file : directory.listFiles()) {
            if (file.isHidden()) continue;

            if (file.isDirectory()) {
                processDirectory(file);
            } else {
                FileDocument fileDocument = createFileDocument(file);
                fileRepository.save(fileDocument);
            }
        }
    }

    public void processFile(String filePath) throws IOException, NoSuchAlgorithmException {
        File file = new File(filePath);

        if (file.isDirectory()) {
            processDirectory(file);
        } else {
            FileDocument fileDocument = createFileDocument(file);
            fileRepository.save(fileDocument);
        }
    }

    public String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            if (document.isEncrypted()) {
                throw new IOException("The PDF is encrypted and cannot be processed.");
            }
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private FileDocument createFileDocument(File file) throws IOException, NoSuchAlgorithmException {
        FileDocument document = new FileDocument();
        document.setFileName(file.getName());
        document.setFilePath(file.getAbsolutePath());
        document.setSize(file.length());
        document.setHash(computeFileHash(file));

        if (file.getName().toLowerCase().endsWith(".txt")
                || file.getName().toLowerCase().endsWith(".md")
                || file.getName().toLowerCase().endsWith(".csv")
                || file.getName().toLowerCase().endsWith(".json")) {
            String content = new String(Files.readAllBytes(file.toPath()));
            document.setContent(content);
        }
        else if(file.getName().toLowerCase().endsWith(".pdf")) {
            try {
                String content = extractTextFromPdf(file);
                document.setContent(content);
            } catch (IOException e) {
                System.err.println("Failed to extract text from PDF: " + file.getName());
                document.setContent(null);
            }

        }else {
            document.setContent(null);
        }

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
