package com.to;

import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import com.to.service.FileAnalysisService;
import com.to.service.FileManagementService;
import com.to.service.FileProcessingService;
import com.to.service.FileService;
import org.junit.jupiter.api.AfterEach;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    private FileService fileService;

    private FileDocument file1 = new FileDocument();
    private FileDocument file2 = new FileDocument();
    private FileDocument file3 = new FileDocument();
    private FileDocument file4 = new FileDocument();
    private FileDocument file5 = new FileDocument();

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        FileAnalysisService fileAnalysisService = Mockito.spy(new FileAnalysisService(fileRepository));
        FileProcessingService fileProcessingService = Mockito.spy(new FileProcessingService(fileRepository));
        FileManagementService fileManagementService = Mockito.spy(new FileManagementService(fileRepository));
        fileService = new FileService(fileProcessingService, fileManagementService, fileAnalysisService);
    }

    @AfterEach
    void cleanUp() {
        file1 = new FileDocument();
        file2 = new FileDocument();
        file3 = new FileDocument();
        file4 = new FileDocument();
        file5 = new FileDocument();
    }

    @Test
    void testFindDuplicates() {
        // given
        file1.setHash("hash123");
        file1.setFileName("file1.txt");

        file2.setHash("hash123");
        file2.setFileName("file2.txt");

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
        file1.setFileName("file1.txt");

        file2.setFileName("file1_v1.txt");

        file3.setFileName("file2.txt");

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
        file1.setFileName("file.txt");
        file1.setSize(10);

        file2.setFileName("file.txt");
        file2.setSize(20);

        file3.setFileName("file.txt");
        file3.setSize(30);

        file4.setFileName("file.txt");
        file4.setSize(30);

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
            Path dirPart = Path.of("source");
            Assertions.assertNotNull(zipFile.getEntry(String.valueOf(dirPart.resolve("file1.txt"))));
            Assertions.assertNotNull(zipFile.getEntry(String.valueOf(dirPart.resolve("file2.txt"))));
            Assertions.assertEquals(3, zipFile.size());
        }
    }

    @Test
    void moveFilesToDirectory() throws IOException {
        // given
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectory(sourceDir);

        Files.writeString(sourceDir.resolve("file1.txt"), "Hello, world!");
        Files.writeString(sourceDir.resolve("file2.txt"), "Test data");


        file1.setId("1");
        file1.setFileName("file1.txt");
        file1.setFilePath(sourceDir.resolve("file1.txt").toString());

        file2.setId("2");
        file2.setFileName("file2.txt");
        file2.setFilePath(sourceDir.resolve("file2.txt").toString());

        List<FileDocument> allFiles = List.of(file1, file2);
        Mockito.when(fileRepository.findAllById(List.of(file1.getId(), file2.getId()))).thenReturn(allFiles);
        Path targetDir = tempDir.resolve("target");

        // when
        fileService.moveSelectedFilesToDirectory(List.of(file1.getId(), file2.getId()), targetDir.toString());

        // then
        File targetDirectory = new File(targetDir.toString());
        Assertions.assertTrue(targetDirectory.exists());
        Assertions.assertTrue(targetDirectory.isDirectory());
        Assertions.assertTrue(new File(targetDir + "/file1.txt").exists());
        Assertions.assertTrue(new File(targetDir + "/file2.txt").exists());
    }

    @Test
    void testDeleteFile() throws IOException {
        // given
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectory(sourceDir);

        file1.setId("1");
        file1.setFileName("file1.txt");
        file1.setFilePath(sourceDir.resolve("file1.txt").toString());

        Files.writeString(sourceDir.resolve("file1.txt"), "Hello, world!");

        Mockito.when(fileRepository.findById(file1.getId())).thenReturn(java.util.Optional.of(file1));

        // when
        fileService.deleteFile(file1.getId());

        // then
        Assertions.assertFalse(Files.exists(sourceDir.resolve("file1.txt")));
    }

    @Test
    void testOpenFile() throws IOException {
        // given
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectory(sourceDir);
        Files.writeString(sourceDir.resolve("file1.txt"), "Hello, world!");


        // when
        fileService.openFile(sourceDir.resolve("file1.txt").toString());

        // then
        Assertions.assertTrue(Files.exists(sourceDir.resolve("file1.txt")));
        Assertions.assertDoesNotThrow(() -> fileService.openFile(sourceDir.resolve("file1.txt").toString()));
    }

    @Test
    void testMoveDuplicatesToGroupedDirectories() throws IOException {
        // given
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectory(sourceDir);
        Files.writeString(sourceDir.resolve("file1.txt"), "Hello, world!");
        Files.writeString(sourceDir.resolve("file2.txt"), "Hello, world!");

        file1.setId("1");
        file1.setFileName("file1.txt");
        file1.setHash("hash123");
        file1.setFilePath(sourceDir.resolve("file1.txt").toString());

        file2.setId("2");
        file2.setFileName("file2.txt");
        file2.setHash("hash123");
        file2.setFilePath(sourceDir.resolve("file2.txt").toString());

        file3.setId("3");
        file3.setFileName("file33131341.txt");
        file3.setHash("hash45623131");
        file3.setFilePath(sourceDir.resolve("file33131341.txt").toString());

        List<FileDocument> allFiles = List.of(file1, file2, file3);
        Mockito.when(fileRepository.findAll()).thenReturn(allFiles);

        // when
        fileService.moveDuplicatesToGroupedDirectories(sourceDir.toString());

        // then
        Assertions.assertTrue(Files.exists(sourceDir.resolve("duplicates1/file1.txt")));
        Assertions.assertTrue(Files.exists(sourceDir.resolve("duplicates1/file2.txt")));
        Assertions.assertFalse(Files.exists(sourceDir.resolve("duplicates1/file33131341.txt")));
    }

    @Test
    void testMoveVersionsToGroupedDirectories() throws IOException {
        // given
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectory(sourceDir);
        Files.writeString(sourceDir.resolve("file1.txt"), "Hello, world!");
        Files.writeString(sourceDir.resolve("file1_v1.txt"), "Hello, world!");
        Files.writeString(sourceDir.resolve("file2.txt"), "Hello, world!");
        Files.writeString(sourceDir.resolve("file-kopia.txt"), "Hello, world!");
        Files.writeString(sourceDir.resolve("file1_copy.txt"), "Hello, world!");

        file1.setId("1");
        file1.setFileName("file1.txt");
        file1.setFilePath(sourceDir.resolve("file1.txt").toString());

        file2.setId("2");
        file2.setFileName("file1_v1.txt");
        file2.setFilePath(sourceDir.resolve("file1_v1.txt").toString());

        file3.setId("3");
        file3.setFileName("file2.txt");
        file3.setFilePath(sourceDir.resolve("file2.txt").toString());

        file4.setId("4");
        file4.setFileName("file3123-kopia.txt");
        file4.setFilePath(sourceDir.resolve("file3123-kopia.txt").toString());

        file5.setId("5");
        file5.setFileName("file1_copy.txt");
        file5.setFilePath(sourceDir.resolve("file1_copy.txt").toString());

        List<FileDocument> allFiles = List.of(file1, file2, file3, file4, file5);
        Mockito.when(fileRepository.findAll()).thenReturn(allFiles);

        // when
        fileService.moveVersionsToGroupedDirectories(sourceDir.toString(), 3);

        // then
        Assertions.assertTrue(Files.exists(sourceDir.resolve("versions1/file1.txt")));
        Assertions.assertTrue(Files.exists(sourceDir.resolve("versions1/file1_v1.txt")));
        Assertions.assertTrue(Files.exists(sourceDir.resolve("versions1/file1_copy.txt")));
        Assertions.assertTrue(Files.exists(sourceDir.resolve("versions1/file2.txt")));
        Assertions.assertFalse(Files.exists(sourceDir.resolve("versions1/file3123-kopia.txt")));
    }
}
