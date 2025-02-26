package com.to;

import com.to.logic.ActionStatus;
import com.to.logic.ActionType;
import com.to.model.ActionLog;
import com.to.model.FileDocument;
import com.to.repository.ActionLogRepository;
import com.to.repository.FileRepository;
import com.to.service.ActionLogService;
import com.to.service.FileProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionLogServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private ActionLogRepository actionLogRepository;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private ActionLogService actionLogService;

    @Mock
    private FileProcessingService fileProcessingService;

//    @BeforeEach
//    void setUp() {
//        fileProcessingService = new FileProcessingService(fileRepository);
//        actionLogService = new ActionLogService(actionLogRepository, fileRepository, fileProcessingService);
//    }

    @Test
    void shouldLogOpenFileSuccessfully() {
        // given
        String filePath = "/path/to/file.txt";
        boolean isSuccessful = true;

        // when
        actionLogService.logOpenFile(filePath, isSuccessful);

        // then
        ArgumentCaptor<ActionLog> actionLogCaptor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository, times(1)).save(actionLogCaptor.capture());

        ActionLog savedLog = actionLogCaptor.getValue();
        assertEquals(filePath, savedLog.getFilePath());
        assertEquals(ActionStatus.SUCCESS.toString(), savedLog.getStatus());
        assertEquals(String.valueOf(ActionType.OPEN_FILE), savedLog.getActionType());
    }

    @Test
    void shouldLogDeleteFileSuccessfully() throws IOException {
        // given
        String fileId = "12345";
        FileDocument fileDocument = new FileDocument();
        fileDocument.setId(fileId);
        String filePath = tempDir.resolve("file.txt").toString();
        Files.writeString(Path.of(filePath), "content");
        fileDocument.setFilePath(filePath);

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileDocument));
        byte[] fileBytes = Files.readAllBytes(Path.of(fileDocument.getFilePath()));
        String encodedContent = Base64.getEncoder().encodeToString(fileBytes);

        // when
        String logId = actionLogService.logDeleteFile(fileId);

        // then
        ArgumentCaptor<ActionLog> actionLogCaptor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository, times(1)).save(actionLogCaptor.capture());

        ActionLog savedLog = actionLogCaptor.getValue();
        assertEquals(fileDocument.getFilePath(), savedLog.getFilePath());
        assertEquals(encodedContent, savedLog.getFileContentBase64());
        assertEquals(ActionStatus.PENDING.toString(), savedLog.getStatus());
        assertEquals(String.valueOf(ActionType.DELETE_FILE), savedLog.getActionType());
    }

    @Test
    void shouldChangeLogStatus() {
        // given
        String logId = "log123";
        ActionStatus newStatus = ActionStatus.SUCCESS;
        ActionLog actionLog = new ActionLog();
        actionLog.setId(logId);
        actionLog.setStatus(ActionStatus.PENDING.toString());

        when(actionLogRepository.findById(logId)).thenReturn(Optional.of(actionLog));

        // when
        actionLogService.changeLogStatus(logId, newStatus);

        // then
        verify(actionLogRepository, times(1)).save(actionLog);
        assertEquals(newStatus.toString(), actionLog.getStatus());
    }

    @Test
    void shouldLogOpenFileFailure() {
        // given
        String filePath = "/path/to/file.txt";
        boolean isSuccessful = false;

        // when
        actionLogService.logOpenFile(filePath, isSuccessful);

        // then
        ArgumentCaptor<ActionLog> actionLogCaptor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository, times(1)).save(actionLogCaptor.capture());

        ActionLog savedLog = actionLogCaptor.getValue();
        assertEquals(filePath, savedLog.getFilePath());
        assertEquals(ActionStatus.FAILURE.toString(), savedLog.getStatus());
        assertEquals(String.valueOf(ActionType.OPEN_FILE), savedLog.getActionType());
    }

    @Test
    void shouldLogDeleteFileFailure() {
        // given
        String fileId = "12345";
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> actionLogService.logDeleteFile(fileId));

        // then
        assertEquals("File not found: " + fileId, exception.getMessage());
        verify(actionLogRepository, never()).save(any(ActionLog.class));
    }

    @Test
    void shouldRevertDeleteFileAction() throws IOException, NoSuchAlgorithmException {
        // given
        String actionLogId = "log123";
        ActionLog actionLog = new ActionLog();
        actionLog.setId(actionLogId);
        actionLog.setActionType("DELETE_FILE");
        actionLog.setFilePath(tempDir.resolve("file.txt").toString());
        actionLog.setFileContentBase64(Base64.getEncoder().encodeToString("file content".getBytes()));

        when(actionLogRepository.findById(actionLogId)).thenReturn(Optional.of(actionLog));

        // when
        actionLogService.revertAction(actionLogId);

        // then
        verify(actionLogRepository, atLeastOnce()).findById(actionLogId);
        assertTrue(Files.exists(Path.of(actionLog.getFilePath())));
        assertEquals("file content", Files.readString(Path.of(actionLog.getFilePath())));
        verify(fileProcessingService, times(1)).processFile(actionLog.getFilePath());
    }

    @Test
    void shouldRevertMoveFilesAction() throws IOException, NoSuchAlgorithmException {
        // given
        String actionLogId = "log124";
        ActionLog actionLog = new ActionLog();

        Path sourceDir = tempDir.resolve("sourceDir");
        Path targetDir = tempDir.resolve("targetDir");

        actionLog.setId(actionLogId);
        actionLog.setActionType("MOVE_FILES");
        actionLog.setFilePath(sourceDir.resolve("file.txt").toString());
        actionLog.setTargetPath(targetDir.resolve("file.txt").toString());

        Files.createDirectory(tempDir.resolve("sourceDir"));
        Files.createDirectory(tempDir.resolve("targetDir"));
        Files.createFile(Path.of(targetDir.toString(), "file.txt"));

        when(actionLogRepository.findById(actionLogId)).thenReturn(Optional.of(actionLog));

        // when
        actionLogService.revertAction(actionLogId);

        // then
        verify(actionLogRepository, atLeastOnce()).findById(actionLogId);
        assertTrue(Files.exists(sourceDir.resolve("file.txt")));
        assertFalse(Files.exists(targetDir.resolve("file.txt")));
    }


    @Test
    void shouldLogMoveFilesSuccessfully() {
        // given
        String sourceDirectoryPath = tempDir.resolve("sourceDir").toString();
        String targetDirectoryPath = tempDir.resolve("targetDir").toString();
        boolean isSuccessful = true;
        boolean isArchived = false;

        // when
        String logId = actionLogService.logMoveFiles(sourceDirectoryPath, targetDirectoryPath, isSuccessful, isArchived);

        // then
        ArgumentCaptor<ActionLog> actionLogCaptor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository, times(1)).save(actionLogCaptor.capture());

        ActionLog savedLog = actionLogCaptor.getValue();
        assertEquals(sourceDirectoryPath, savedLog.getFilePath());
        assertEquals(targetDirectoryPath, savedLog.getTargetPath());
        assertEquals(ActionStatus.SUCCESS.toString(), savedLog.getStatus());
        assertEquals(String.valueOf(ActionType.MOVE_FILES), savedLog.getActionType());
        assertFalse(savedLog.isArchived());
    }

    @Test
    void shouldLogArchiveFilesSuccessfully() {
        // given
        String sourceDirectoryPath = tempDir.resolve("sourceDir").toString();
        String targetDirectoryPath = tempDir.resolve("targetDir").toString();
        boolean isSuccessful = true;
        boolean isArchived = true;

        // when
        String logId = actionLogService.logArchiveFiles(sourceDirectoryPath, targetDirectoryPath, isSuccessful, isArchived);

        // then
        ArgumentCaptor<ActionLog> actionLogCaptor = ArgumentCaptor.forClass(ActionLog.class);
        verify(actionLogRepository, times(1)).save(actionLogCaptor.capture());

        ActionLog savedLog = actionLogCaptor.getValue();
        assertEquals(sourceDirectoryPath, savedLog.getFilePath());
        assertEquals(targetDirectoryPath, savedLog.getTargetPath());
        assertEquals(ActionStatus.SUCCESS.toString(), savedLog.getStatus());
        assertEquals(String.valueOf(ActionType.ARCHIVE_FILES), savedLog.getActionType());
        assertTrue(savedLog.isArchived());
    }

    @Test
    void shouldRevertArchiveFilesAction() throws IOException, NoSuchAlgorithmException {
        // given
        String actionLogId = "log125";
        ActionLog actionLog = new ActionLog();
        actionLog.setId(actionLogId);
        actionLog.setActionType("ARCHIVE_FILES");
        actionLog.setFilePath(tempDir.resolve("targetDir").toString());
        actionLog.setTargetPath(tempDir.resolve("archive.zip").toString());

        Path zipFilePath = Path.of(actionLog.getTargetPath());
        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
            ZipOutputStream zos = new ZipOutputStream(fos)) {
            ZipEntry zipEntry = new ZipEntry("file.txt");
            zos.putNextEntry(zipEntry);
            zos.write("file content".getBytes());
            zos.closeEntry();
        }

        when(actionLogRepository.findById(actionLogId)).thenReturn(Optional.of(actionLog));

        // when
        actionLogService.revertAction(actionLogId);

        // then
        verify(actionLogRepository, atLeastOnce()).findById(actionLogId);
        assertTrue(Files.exists(Path.of(actionLog.getFilePath(), "file.txt")));
        assertFalse(Files.exists(zipFilePath));
    }
}