package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTest {

    @Test
    void defaultConstructor_shouldInitializeUploadDirectory() {
        new FileStorageService();
    }

    @Test
    void constructor_shouldWrapIOException_whenUploadPathIsFile() throws IOException {
        Path uploadPathFile = Files.createTempFile("moviecat-upload-file", ".tmp");

        assertThrows(UncheckedIOException.class, () -> new FileStorageService(uploadPathFile));
    }

    @Test
    void isValidExtension_shouldReturnFalse_forNullViaReflection() throws ReflectiveOperationException {
        FileStorageService service = new FileStorageService();
        Method method = FileStorageService.class.getDeclaredMethod("isValidExtension", String.class);
        method.setAccessible(true);

        Object result = method.invoke(service, new Object[] {null});

        assertTrue(Boolean.FALSE.equals(result));
    }

    @Test
    void storeFile_shouldSaveFileWithAllowedExtension() throws IOException {
        Path uploadDir = Files.createTempDirectory("moviecat-upload-dir");
        FileStorageService service = new FileStorageService(uploadDir);
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("poster.jpg");
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream("img".getBytes(StandardCharsets.UTF_8)));

        String storedFilename = service.storeFile(file);

        assertTrue(storedFilename.endsWith(".jpg"));
        assertTrue(Files.exists(uploadDir.resolve(storedFilename)));
    }

    @Test
    void storeFile_shouldThrowForEmptyFile() throws IOException {
        FileStorageService service = new FileStorageService(Files.createTempDirectory("moviecat-upload-dir"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file));
    }

    @Test
    void storeFile_shouldThrowForInvalidExtension() throws IOException {
        FileStorageService service = new FileStorageService(Files.createTempDirectory("moviecat-upload-dir"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("poster.txt");

        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file));
    }

    @Test
    void storeFile_shouldThrowForMissingExtension() throws IOException {
        FileStorageService service = new FileStorageService(Files.createTempDirectory("moviecat-upload-dir"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("poster");

        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file));
    }

    @Test
    void storeFile_shouldThrowForNullFilename() throws IOException {
        FileStorageService service = new FileStorageService(Files.createTempDirectory("moviecat-upload-dir"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file));
    }

    @Test
    void storeFile_shouldWrapIOException() throws IOException {
        FileStorageService service = new FileStorageService(Files.createTempDirectory("moviecat-upload-dir"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("poster.jpg");
        when(file.getInputStream()).thenThrow(new IOException("boom"));

        assertThrows(UncheckedIOException.class, () -> service.storeFile(file));
    }
}
