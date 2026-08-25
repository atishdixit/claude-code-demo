package com.example.tiffconverter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileScannerTest {

    private final FileScanner scanner = new FileScanner();

    @Test
    void findsTifAndTiffCaseInsensitivelyAndIgnoresOthers(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("a.tiff"), "x");
        Files.writeString(dir.resolve("B.TIF"), "x");
        Files.writeString(dir.resolve("c.txt"), "x");
        Files.writeString(dir.resolve("d.jpg"), "x");
        Files.writeString(dir.resolve("e.tiffx"), "x"); // deliberately not a real .tiff extension

        List<Path> found = scanner.findTiffFiles(dir);

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(p -> p.getFileName().toString().equals("a.tiff")));
        assertTrue(found.stream().anyMatch(p -> p.getFileName().toString().equals("B.TIF")));
    }

    @Test
    void returnsEmptyListForEmptyDirectory(@TempDir Path dir) throws Exception {
        assertEquals(0, scanner.findTiffFiles(dir).size());
    }

    @Test
    void ignoresSubdirectoriesEvenIfNamedLikeATiff(@TempDir Path dir) throws Exception {
        Files.createDirectory(dir.resolve("looks-like-a.tiff"));

        assertEquals(0, scanner.findTiffFiles(dir).size());
    }
}
