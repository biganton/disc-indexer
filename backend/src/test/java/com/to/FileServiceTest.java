package com.to;

import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import com.to.service.FileAnalysisService;
import com.to.service.FileManagementService;
import com.to.service.FileProcessingService;
import com.to.service.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        FileAnalysisService fileAnalysisService = Mockito.spy(new FileAnalysisService(fileRepository));
        FileProcessingService fileProcessingService = Mockito.spy(new FileProcessingService(fileRepository));
        FileManagementService fileManagementService = Mockito.spy(new FileManagementService(fileRepository));
        fileService = new FileService(fileProcessingService, fileManagementService, fileAnalysisService);
    }

    @Test
    void testFindDuplicates() {
        // given
        FileDocument file1 = new FileDocument();
        file1.setHash("hash123");
        file1.setFileName("file1.txt");

        FileDocument file2 = new FileDocument();
        file2.setHash("hash123");
        file2.setFileName("file2.txt");

        FileDocument file3 = new FileDocument();
        file3.setHash("hash456");
        file3.setFileName("file3.txt");

        List<FileDocument> allFiles = List.of(file1, file2, file3);

        Mockito.when(fileRepository.findAll()).thenReturn(allFiles);

        // when
        List<List<FileDocument>> duplicates = fileService.findDuplicates();

        // then
        Assertions.assertEquals(1, duplicates.size());
        Assertions.assertEquals(2, duplicates.getFirst().size());
        Assertions.assertTrue(duplicates.getFirst().contains(file1));
        Assertions.assertTrue(duplicates.getFirst().contains(file2));
    }

    @Test
    void testFindFileVersions() {
        // given
        FileDocument file1 = new FileDocument();
        file1.setFileName("file1.txt");

        FileDocument file2 = new FileDocument();
        file2.setFileName("file1_v1.txt");

        FileDocument file3 = new FileDocument();
        file3.setFileName("file2.txt");

        FileDocument file4 = new FileDocument();
        file4.setFileName("file-kopia.txt");

        List<FileDocument> allFiles = List.of(file1, file2, file3, file4);
        Mockito.when(fileRepository.findAll()).thenReturn(allFiles);

        // when
        List<List<FileDocument>> versions = fileService.findFileVersions(2);

        // then
        Assertions.assertEquals(1, versions.size());
        Assertions.assertEquals(3, versions.getFirst().size());
        Assertions.assertTrue(versions.getFirst().contains(file1));
        Assertions.assertTrue(versions.getFirst().contains(file3));
        Assertions.assertTrue(versions.getFirst().contains(file4));
    }

    @Test
    void testFindLargestFiles() {
        //given
        FileDocument file1 = new FileDocument();
        file1.setFileName("file.txt");
        file1.setSize(10);

        FileDocument file2 = new FileDocument();
        file2.setFileName("file.txt");
        file2.setSize(20);

        FileDocument file3 = new FileDocument();
        file3.setFileName("file.txt");
        file3.setSize(30);

        FileDocument file4 = new FileDocument();
        file4.setFileName("file.txt");
        file4.setSize(30);

        FileDocument file5 = new FileDocument();
        file5.setFileName("file.txt");
        file5.setSize(50);

        List<FileDocument> allFiles = List.of(file1, file2, file3, file4, file5);
        Mockito.when(fileRepository.findAll()).thenReturn(allFiles);

        // when
        List<FileDocument> biggestFiles = fileService.findLargestFiles(3);

        // then
        Assertions.assertEquals(3, biggestFiles.size());
        Assertions.assertEquals(50, biggestFiles.getFirst().getSize());
        Assertions.assertTrue(biggestFiles.contains(file5));
        Assertions.assertTrue(biggestFiles.contains(file4));
        Assertions.assertTrue(biggestFiles.contains(file3));
    }

    @Test
    void testArchiving() throws IOException {
        // given
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectory(sourceDir);

        Files.writeString(sourceDir.resolve("file1.txt"), "Hello, world!");
        Files.writeString(sourceDir.resolve("file2.txt"), "Test data");

        Path zipFilePath = tempDir.resolve("archive.zip");

        // when
        fileService.archiveDirectory(sourceDir.toString(), zipFilePath.toString());

        // then
        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
//            Enumeration<? extends ZipEntry> entries = zipFile.entries();
//            while(entries.hasMoreElements()){
//                System.out.println(entries.nextElement());
//            }
            Path dirPart = Path.of("source");
            Assertions.assertNotNull(zipFile.getEntry(String.valueOf(dirPart.resolve("file1.txt"))));
            Assertions.assertNotNull(zipFile.getEntry(String.valueOf(dirPart.resolve("file2.txt"))));
            Assertions.assertEquals(3, zipFile.size());
        }
    }
}
