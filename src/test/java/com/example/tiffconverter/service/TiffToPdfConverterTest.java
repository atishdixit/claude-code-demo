package com.example.tiffconverter.service;

import com.example.tiffconverter.exception.TiffConversionException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TiffToPdfConverterTest {

    private final TiffToPdfConverter converter = new TiffToPdfConverter();

    @Test
    void convertsSinglePageTiff(@TempDir Path tempDir) throws Exception {
        Path tiff = tempDir.resolve("single.tiff");
        TiffFixtureGenerator.generateSinglePage(tiff, 300, 200, "hello");
        Path pdf = tempDir.resolve("single.pdf");

        int pages = converter.convert(tiff, pdf);

        assertEquals(1, pages);
        assertTrue(Files.exists(pdf));
        assertEquals(1, countPdfPages(pdf));
    }

    @Test
    void convertsMultiPageTiffPreservingPageCount(@TempDir Path tempDir) throws Exception {
        Path tiff = tempDir.resolve("multi.tiff");
        TiffFixtureGenerator.generateMultiPage(tiff, 4, 300, 200);
        Path pdf = tempDir.resolve("multi.pdf");

        int pages = converter.convert(tiff, pdf);

        assertEquals(4, pages);
        assertEquals(4, countPdfPages(pdf));
    }

    @Test
    void convertsMockPatentReportPreservingAllFourPages(@TempDir Path tempDir) throws Exception {
        Path tiff = tempDir.resolve("patent.tiff");
        TiffFixtureGenerator.generatePatentReport(tiff);
        Path pdf = tempDir.resolve("patent.pdf");

        int pages = converter.convert(tiff, pdf);

        assertEquals(4, pages, "title page + abstract + claims + description");
        assertEquals(4, countPdfPages(pdf));
    }

    @Test
    void corruptFileThrowsAndLeavesNoPartialOutput(@TempDir Path tempDir) throws Exception {
        Path fakeTiff = tempDir.resolve("corrupt.tiff");
        TiffFixtureGenerator.generateCorrupt(fakeTiff);
        Path pdf = tempDir.resolve("corrupt.pdf");

        assertThrows(TiffConversionException.class, () -> converter.convert(fakeTiff, pdf));
        assertFalse(Files.exists(pdf), "no partial/empty PDF should be left behind after a failed conversion");
    }

    @Test
    void missingFileThrows(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.tiff");
        Path pdf = tempDir.resolve("out.pdf");

        assertThrows(TiffConversionException.class, () -> converter.convert(missing, pdf));
    }

    @Test
    void emptyFileThrows(@TempDir Path tempDir) throws IOException {
        Path emptyTiff = tempDir.resolve("empty.tiff");
        Files.createFile(emptyTiff);
        Path pdf = tempDir.resolve("empty.pdf");

        assertThrows(TiffConversionException.class, () -> converter.convert(emptyTiff, pdf));
    }

    private int countPdfPages(Path pdf) throws IOException {
        try (PdfDocument doc = new PdfDocument(new PdfReader(pdf.toString()))) {
            return doc.getNumberOfPages();
        }
    }
}
