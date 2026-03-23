package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTest {

    @Test
    void defaultConstructor_shouldInitializeUploadDirectory() {
        FileStorageService service = new FileStorageService();
        assertNotNull(service);
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

        assertEquals(Boolean.FALSE, result);
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

    @ParameterizedTest
    @MethodSource("invalidFilenames")
    void storeFile_shouldThrowForInvalidFilename(String originalFilename) throws IOException {
        FileStorageService service = new FileStorageService(Files.createTempDirectory("moviecat-upload-dir"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(originalFilename);

        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file));
    }

    private static Stream<String> invalidFilenames() {
        return Stream.of("poster.txt", "poster", null);
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
