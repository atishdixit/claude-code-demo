package com.example.tiffconverter.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Finds .tif / .tiff files (case-insensitive) directly inside a directory. */
public class FileScanner {

    private static final List<String> TIFF_EXTENSIONS = List.of(".tif", ".tiff");

    /**
     * @return sorted list of matching files, or an empty list if the directory
     *         has no matches. Never returns null.
     */
    public List<Path> findTiffFiles(Path directory) throws IOException {
        List<Path> results = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path) && hasTiffExtension(path)) {
                    results.add(path);
                }
            }
        }
        results.sort(Comparator.comparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    private boolean hasTiffExtension(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return TIFF_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
