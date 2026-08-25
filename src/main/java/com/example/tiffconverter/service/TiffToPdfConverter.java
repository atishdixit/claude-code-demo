package com.example.tiffconverter.service;

import com.example.tiffconverter.exception.TiffConversionException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts one TIFF file (single- or multi-page) into one PDF, one PDF page
 * per TIFF frame, each PDF page sized to exactly match its source image.
 */
public class TiffToPdfConverter {

    private static final Logger log = LoggerFactory.getLogger(TiffToPdfConverter.class);

    /**
     * @return the number of pages written to the output PDF.
     * @throws TiffConversionException on any failure reading the TIFF or
     *         writing the PDF -- always wraps the root cause.
     */
    public int convert(Path tiffFile, Path outputPdf) throws TiffConversionException {
        byte[] tiffBytes = readTiffBytes(tiffFile);
        int pageCount = countPages(tiffFile, tiffBytes);

        try (PdfWriter writer = new PdfWriter(outputPdf.toString());
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            for (int page = 1; page <= pageCount; page++) {
                writeImagePage(pdfDoc, tiffFile, tiffBytes, page);
            }
            return pageCount;

        } catch (TiffConversionException e) {
            deletePartialOutput(outputPdf);
            throw e;
        } catch (Exception e) {
            deletePartialOutput(outputPdf);
            throw new TiffConversionException(
                    "Failed to write PDF for '" + tiffFile.getFileName() + "': " + e.getMessage(), e);
        }
    }

    private byte[] readTiffBytes(Path tiffFile) throws TiffConversionException {
        try {
            byte[] bytes = Files.readAllBytes(tiffFile);
            if (bytes.length == 0) {
                throw new TiffConversionException("File '" + tiffFile.getFileName() + "' is empty.");
            }
            return bytes;
        } catch (IOException e) {
            throw new TiffConversionException(
                    "Could not read '" + tiffFile.getFileName() + "': " + e.getMessage(), e);
        }
    }

    /** iText has no direct "page count" call, so we probe: try decoding page N+1 until it fails. */
    private int countPages(Path tiffFile, byte[] tiffBytes) throws TiffConversionException {
        // First page must succeed, or this isn't a readable TIFF at all.
        try {
            ImageDataFactory.createTiff(tiffBytes, true, 1, false);
        } catch (Exception e) {
            throw new TiffConversionException(
                    "'" + tiffFile.getFileName() + "' is not a valid/readable TIFF file: " + e.getMessage(), e);
        }

        int page = 2;
        while (true) {
            try {
                ImageDataFactory.createTiff(tiffBytes, true, page, false);
                page++;
            } catch (Exception e) {
                return page - 1;
            }
        }
    }

    private void writeImagePage(PdfDocument pdfDoc, Path tiffFile, byte[] tiffBytes, int pageNumber)
            throws TiffConversionException {
        ImageData imageData;
        try {
            imageData = ImageDataFactory.createTiff(tiffBytes, true, pageNumber, false);
        } catch (Exception e) {
            throw new TiffConversionException(
                    "Could not decode page " + pageNumber + " of '" + tiffFile.getFileName() + "': " + e.getMessage(), e);
        }

        float width = imageData.getWidth();
        float height = imageData.getHeight();

        PdfPage pdfPage = pdfDoc.addNewPage(new PageSize(width, height));
        PdfCanvas canvas = new PdfCanvas(pdfPage);
        canvas.addImageFittedIntoRectangle(imageData, new Rectangle(0, 0, width, height), false);

        log.info("Rendered page {} ({}x{}px) of '{}'", pageNumber, (int) width, (int) height, tiffFile.getFileName());
    }

    private void deletePartialOutput(Path outputPdf) {
        try {
            Files.deleteIfExists(outputPdf);
        } catch (IOException e) {
            log.warn("Could not clean up partially-written file '{}': {}", outputPdf, e.getMessage());
        }
    }
}
