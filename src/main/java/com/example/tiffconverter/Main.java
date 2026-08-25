package com.example.tiffconverter;

import com.example.tiffconverter.config.AppConfig;
import com.example.tiffconverter.service.BatchConverter;
import com.example.tiffconverter.service.ConversionSummary;
import com.example.tiffconverter.service.FileScanner;
import com.example.tiffconverter.service.TiffToPdfConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Entry point. Exit codes (useful if run.bat is ever wired into a scheduled
 * task / CI step): 0 = all converted (or nothing to do), 1 = one or more
 * files failed to convert, 2 = the run couldn't start at all (e.g. folders
 * couldn't be created).
 */
public final class Main {

    public static void main(String[] args) {
        try {
            Files.createDirectories(AppConfig.LOGS_DIR);
        } catch (IOException e) {
            System.err.println("FATAL: could not create logs directory '" + AppConfig.LOGS_DIR + "': " + e.getMessage());
            System.exit(2);
            return;
        }

        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("=== tiff-to-pdf-converter starting ===");
        log.info("input='{}' output='{}' logs='{}'", AppConfig.INPUT_DIR, AppConfig.OUTPUT_DIR, AppConfig.LOGS_DIR);

        try {
            BatchConverter batchConverter = new BatchConverter(new FileScanner(), new TiffToPdfConverter());
            ConversionSummary summary = batchConverter.run(AppConfig.INPUT_DIR, AppConfig.OUTPUT_DIR);

            log.info("=== tiff-to-pdf-converter finished: found={}, succeeded={}, failed={} ===",
                    summary.found(), summary.succeeded(), summary.failed());

            System.exit(summary.hasFailures() ? 1 : 0);
        } catch (Exception e) {
            log.error("Run aborted by an unexpected error: {}", e.getMessage(), e);
            System.exit(2);
        }
    }

    private Main() {
    }
}
