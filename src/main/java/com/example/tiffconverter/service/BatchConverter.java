package com.example.tiffconverter.service;

import com.example.tiffconverter.exception.TiffConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Orchestrates a full run: make sure the folders exist, find every TIFF in
 * the input folder, convert each one independently. One bad file is logged
 * and skipped -- it never aborts the rest of the batch.
 */
public class BatchConverter {

    private static final Logger log = LoggerFactory.getLogger(BatchConverter.class);

    private final FileScanner fileScanner;
    private final TiffToPdfConverter converter;

    public BatchConverter(FileScanner fileScanner, TiffToPdfConverter converter) {
        this.fileScanner = fileScanner;
        this.converter = converter;
    }

    public ConversionSummary run(Path inputDir, Path outputDir) throws IOException {
        ensureDirectoryExists(outputDir);

        if (!Files.exists(inputDir)) {
            Files.createDirectories(inputDir);
            log.warn("Input directory '{}' did not exist -- created it. Nothing to convert this run.", inputDir);
            return new ConversionSummary(0, 0, 0);
        }
        if (!Files.isDirectory(inputDir)) {
            throw new IOException("Input path '" + inputDir + "' exists but is not a directory.");
        }

        List<Path> tiffFiles = fileScanner.findTiffFiles(inputDir);
        if (tiffFiles.isEmpty()) {
            log.warn("No .tif/.tiff files found in '{}'.", inputDir);
            return new ConversionSummary(0, 0, 0);
        }

        log.info("Found {} TIFF file(s) in '{}'.", tiffFiles.size(), inputDir);

        int succeeded = 0;
        int failed = 0;

        for (Path tiffFile : tiffFiles) {
            Path outputPdf = outputDir.resolve(stripExtension(tiffFile.getFileName().toString()) + ".pdf");
            long start = System.currentTimeMillis();
            try {
                int pages = converter.convert(tiffFile, outputPdf);
                long elapsedMs = System.currentTimeMillis() - start;
                log.info("Converted '{}' -> '{}' ({} page(s), {} ms)",
                        tiffFile.getFileName(), outputPdf.getFileName(), pages, elapsedMs);
                succeeded++;
            } catch (TiffConversionException e) {
                log.error("Failed to convert '{}': {}", tiffFile.getFileName(), e.getMessage(), e);
                failed++;
            } catch (Exception e) {
                // Belt-and-suspenders: a single file must never take down the whole batch,
                // even if something unexpected (not a TiffConversionException) slips through.
                log.error("Unexpected error converting '{}': {}", tiffFile.getFileName(), e.getMessage(), e);
                failed++;
            }
        }

        log.info("Batch complete. found={}, succeeded={}, failed={}", tiffFiles.size(), succeeded, failed);
        return new ConversionSummary(tiffFiles.size(), succeeded, failed);
    }

    private void ensureDirectoryExists(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            log.info("Created directory '{}'.", dir);
        } else if (!Files.isDirectory(dir)) {
            throw new IOException("Path '" + dir + "' exists but is not a directory.");
        }
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
