package com.example.tiffconverter.service;

import com.example.tiffconverter.util.TiffFixtureGenerator;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchConverterTest {

    private final BatchConverter batchConverter = new BatchConverter(new FileScanner(), new TiffToPdfConverter());

    @Test
    void mixedValidAndCorruptFiles_convertsGoodOnesAndSkipsBadOnes(@TempDir Path root) throws Exception {
        Path input = root.resolve("input");
        Path output = root.resolve("output");
        Files.createDirectories(input);

        TiffFixtureGenerator.generateSinglePage(input.resolve("good-single.tiff"), 200, 150, "ok");
        TiffFixtureGenerator.generateMultiPage(input.resolve("good-multi.tiff"), 3, 200, 150);
        TiffFixtureGenerator.generateCorrupt(input.resolve("bad.tiff"));
        TiffFixtureGenerator.generateNonTiffFile(input.resolve("ignored.txt"));

        ConversionSummary summary = batchConverter.run(input, output);

        assertEquals(3, summary.found(), "the .txt file must not be counted");
        assertEquals(2, summary.succeeded());
        assertEquals(1, summary.failed());
        assertTrue(summary.hasFailures());

        assertTrue(Files.exists(output.resolve("good-single.pdf")));
        assertEquals(1, countPdfPages(output.resolve("good-single.pdf")));
        assertTrue(Files.exists(output.resolve("good-multi.pdf")));
        assertEquals(3, countPdfPages(output.resolve("good-multi.pdf")));
        assertFalse(Files.exists(output.resolve("bad.pdf")), "a failed conversion must not produce an output file");
    }

    @Test
    void missingInputDirectory_isCreatedAndReturnsZeroWithoutThrowing(@TempDir Path root) throws Exception {
        Path input = root.resolve("does-not-exist-yet");
        Path output = root.resolve("output");

        ConversionSummary summary = batchConverter.run(input, output);

        assertTrue(Files.isDirectory(input), "the input directory should be created for next time");
        assertEquals(0, summary.found());
        assertFalse(summary.hasFailures());
    }

    @Test
    void emptyInputDirectory_returnsZeroWithoutThrowing(@TempDir Path root) throws IOException {
        Path input = root.resolve("input");
        Path output = root.resolve("output");
        Files.createDirectories(input);

        ConversionSummary summary = batchConverter.run(input, output);

        assertEquals(0, summary.found());
        assertEquals(0, summary.succeeded());
        assertEquals(0, summary.failed());
    }

    @Test
    void outputDirectoryIsCreatedIfMissing(@TempDir Path root) throws Exception {
        Path input = root.resolve("input");
        Path output = root.resolve("nested").resolve("output");
        Files.createDirectories(input);
        TiffFixtureGenerator.generateSinglePage(input.resolve("a.tiff"), 100, 100, "a");

        batchConverter.run(input, output);

        assertTrue(Files.isDirectory(output));
        assertTrue(Files.exists(output.resolve("a.pdf")));
    }

    private int countPdfPages(Path pdf) throws IOException {
        try (PdfDocument doc = new PdfDocument(new PdfReader(pdf.toString()))) {
            return doc.getNumberOfPages();
        }
    }
}
