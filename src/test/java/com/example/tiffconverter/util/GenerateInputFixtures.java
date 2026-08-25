package com.example.tiffconverter.util;

import java.nio.file.Path;

/**
 * One-off tool (not a test) that populates the checked-in input/ folder with
 * real sample files, so run.bat has something to convert immediately after a
 * fresh clone -- including one deliberately-broken file to demonstrate error
 * handling. Run via:
 *   mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
 *       -Dexec.mainClass=com.example.tiffconverter.util.GenerateInputFixtures \
 *       -Dexec.classpathScope=test
 */
public final class GenerateInputFixtures {

    public static void main(String[] args) throws Exception {
        Path input = Path.of("input");

        TiffFixtureGenerator.generateSinglePage(
                input.resolve("sample-single-page.tiff"), 400, 250, "Sample single-page TIFF");

        TiffFixtureGenerator.generateMultiPage(
                input.resolve("sample-multi-page.tiff"), 3, 400, 250);

        TiffFixtureGenerator.generateCorrupt(
                input.resolve("sample-corrupt.tiff"));

        TiffFixtureGenerator.generateNonTiffFile(
                input.resolve("sample-not-a-tiff.txt"));

        System.out.println("Sample fixtures written to " + input.toAbsolutePath());
    }

    private GenerateInputFixtures() {
    }
}
