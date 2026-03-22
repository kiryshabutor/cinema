package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTest {

    @Test
    void storeFile_shouldThrowForEmptyFile() {
        FileStorageService service = new FileStorageService();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file));
    }

    @Test
    void storeFile_shouldThrowForInvalidExtension() {
        FileStorageService service = new FileStorageService();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("poster.txt");

        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file));
    }

    @Test
    void storeFile_shouldWrapIOException() throws IOException {
        FileStorageService service = new FileStorageService();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("poster.jpg");
        when(file.getInputStream()).thenThrow(new IOException("boom"));

        assertThrows(UncheckedIOException.class, () -> service.storeFile(file));
    }
}
