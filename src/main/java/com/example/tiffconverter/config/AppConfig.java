package com.example.tiffconverter.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Fixed, relative-to-working-directory folder layout: input/, output/, logs/.
 * Deliberately not configurable via CLI args -- the app is meant to be run
 * by double-clicking run.bat from the project root every time.
 */
public final class AppConfig {

    public static final Path INPUT_DIR = Paths.get("input");
    public static final Path OUTPUT_DIR = Paths.get("output");
    public static final Path LOGS_DIR = Paths.get("logs");

    private AppConfig() {
    }
}
