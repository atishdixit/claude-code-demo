package com.example.tiffconverter.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generates real, valid TIFF files for tests -- and for populating the
 * repo's checked-in input/ sample files -- using the TwelveMonkeys TIFF
 * ImageIO plugin (test-scope only; the app itself never uses ImageIO).
 */
public final class TiffFixtureGenerator {

    private TiffFixtureGenerator() {
    }

    public static void generateSinglePage(Path outFile, int width, int height, String label) throws IOException {
        BufferedImage image = renderPage(width, height, label, Color.BLUE);
        writeSingleFrame(outFile, image);
    }

    public static void generateMultiPage(Path outFile, int pageCount, int width, int height) throws IOException {
        ImageWriter writer = findTiffWriter();
        Files.createDirectories(outFile.getParent());
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outFile.toFile())) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (int i = 1; i <= pageCount; i++) {
                Color color = switch (i % 3) {
                    case 0 -> Color.RED;
                    case 1 -> Color.GREEN;
                    default -> Color.MAGENTA;
                };
                BufferedImage page = renderPage(width, height, "page " + i + " of " + pageCount, color);
                ImageWriteParam param = writer.getDefaultWriteParam();
                IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(page), param);
                writer.writeToSequence(new IIOImage(page, null, metadata), param);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    /**
     * A mock multi-page patent report -- realistic document layout (title/
     * bibliographic page, abstract, claims, description), all placeholder
     * content. Useful as a more realistic-looking scanned-document test
     * fixture than the plain colored-block pages above.
     */
    public static void generatePatentReport(Path outFile) throws IOException {
        int width = 1700;
        int height = 2200; // roughly US Letter at 200dpi

        List<BufferedImage> pages = new ArrayList<>();
        pages.add(renderTitlePage(width, height));
        pages.add(renderTextPage(width, height, "ABSTRACT", ABSTRACT_TEXT, 1));
        pages.add(renderTextPage(width, height, "CLAIMS", CLAIMS_TEXT, 2));
        pages.add(renderTextPage(width, height, "DESCRIPTION", DESCRIPTION_TEXT, 3));

        ImageWriter writer = findTiffWriter();
        Files.createDirectories(outFile.getParent());
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outFile.toFile())) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (BufferedImage page : pages) {
                ImageWriteParam param = deflateParam(writer);
                IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(page), param);
                writer.writeToSequence(new IIOImage(page, null, metadata), param);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    /**
     * Grayscale + Deflate compression -- realistic for a scanned text document
     * (real scanners rarely emit uncompressed 24-bit color for a text page),
     * and keeps this checked-in fixture small (a few hundred KB instead of
     * tens of MB for mostly-white pages).
     */
    private static ImageWriteParam deflateParam(ImageWriter writer) {
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("Deflate");
        }
        return param;
    }

    /** Not a real TIFF at all -- for testing that corrupt/garbage files fail cleanly and don't crash the batch. */
    public static void generateCorrupt(Path outFile) throws IOException {
        Files.createDirectories(outFile.getParent());
        Files.write(outFile, "this is not a real tiff file, just plain text".getBytes(StandardCharsets.UTF_8));
    }

    /** Wrong extension entirely -- for testing that the scanner ignores non-TIFF files. */
    public static void generateNonTiffFile(Path outFile) throws IOException {
        Files.createDirectories(outFile.getParent());
        Files.write(outFile, "not a tiff, should be ignored by the scanner".getBytes(StandardCharsets.UTF_8));
    }

    private static void writeSingleFrame(Path outFile, BufferedImage image) throws IOException {
        Files.createDirectories(outFile.getParent());
        ImageWriter writer = findTiffWriter();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outFile.toFile())) {
            writer.setOutput(ios);
            writer.write(image);
        } finally {
            writer.dispose();
        }
    }

    private static BufferedImage renderPage(int width, int height, String label, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(color);
        g.fillRect(10, 10, width - 20, height - 20);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, height / 8)));
        g.drawString(label, 20, height / 2);
        g.dispose();
        return image;
    }

    private static final String ABSTRACT_TEXT =
            "A system and method are disclosed for automated processing of scanned document images. "
            + "The method includes receiving a plurality of image files in a raster image format, "
            + "identifying discrete pages within each file, converting each page into a fixed-layout "
            + "document format while preserving the original page dimensions, and writing the resulting "
            + "output to a designated storage location. Failures affecting an individual input file are "
            + "isolated so that they do not interrupt processing of the remaining files in the batch. "
            + "This is placeholder text generated for test purposes only and does not describe an actual "
            + "invention.";

    private static final String CLAIMS_TEXT =
            "1. A computer-implemented method comprising: reading a plurality of image files from a "
            + "designated input location; for each image file, determining a number of pages contained "
            + "therein; for each page, generating a corresponding output page sized to match dimensions "
            + "of the page; and writing the output pages to a designated output location.\n\n"
            + "2. The method of claim 1, wherein an error encountered while processing a given image "
            + "file is recorded in a log and does not prevent processing of a subsequent image file.\n\n"
            + "3. The method of claim 1, further comprising creating the designated input location or "
            + "the designated output location if either does not already exist at the time of "
            + "processing.\n\n"
            + "4. The method of claim 1, wherein the plurality of image files are filtered by file "
            + "extension prior to processing.\n\n"
            + "This is placeholder claim language generated for test purposes only.";

    private static final String DESCRIPTION_TEXT =
            "FIELD OF THE INVENTION\n\n"
            + "This disclosure relates generally to document image processing, and more particularly to "
            + "batch conversion of multi-page raster image files into a fixed-layout document format.\n\n"
            + "BACKGROUND\n\n"
            + "Organizations that receive scanned documents often store them as multi-page raster image "
            + "files. Converting a large volume of such files into a widely-distributable document format "
            + "is conventionally a manual or error-prone process, particularly when individual files are "
            + "corrupt or otherwise unreadable.\n\n"
            + "SUMMARY\n\n"
            + "The presently disclosed method addresses this by processing each input file independently, "
            + "so that a single unreadable file is logged and skipped rather than halting the entire batch.\n\n"
            + "This is placeholder description text generated for test purposes only.";

    private static BufferedImage renderTitlePage(int width, int height) {
        BufferedImage image = newPageCanvas(width, height);
        Graphics2D g = graphicsFor(image);

        int margin = width / 12;
        int y = height / 6;

        g.setFont(new Font("Serif", Font.PLAIN, 20));
        g.drawString("(19) United States", margin, y);
        y += 30;
        g.drawString("(12) Patent Application Publication (Mock / Test Fixture)", margin, y);
        y += 60;

        g.setFont(new Font("Serif", Font.BOLD, 34));
        g.drawString("METHOD AND SYSTEM FOR BATCH CONVERSION", margin, y);
        y += 44;
        g.drawString("OF MULTI-PAGE RASTER IMAGES TO A", margin, y);
        y += 44;
        g.drawString("FIXED-LAYOUT DOCUMENT FORMAT", margin, y);
        y += 70;

        g.setFont(new Font("Serif", Font.PLAIN, 22));
        String[] fields = {
                "Pub. No.: US 2026/0123456 A1 (mock, for test purposes only)",
                "Pub. Date: not a real publication",
                "Inventor(s): J. Q. Example, A. Sample",
                "Assignee: Example Test Fixtures LLC",
                "Appl. No.: 00/000,000",
                "Filed: n/a",
        };
        for (String field : fields) {
            g.drawString(field, margin, y);
            y += 34;
        }

        y += 40;
        g.setFont(new Font("Serif", Font.ITALIC, 20));
        for (String line : wrapText(
                "This document is a synthetically generated test fixture for the tiff-to-pdf-converter "
                        + "project. It is not a real patent and describes no actual invention.",
                g.getFontMetrics(), width - margin * 2)) {
            g.drawString(line, margin, y);
            y += 28;
        }

        drawPageBorder(g, width, height);
        g.dispose();
        return image;
    }

    private static BufferedImage renderTextPage(int width, int height, String heading, String body, int pageIndex) {
        BufferedImage image = newPageCanvas(width, height);
        Graphics2D g = graphicsFor(image);

        int margin = width / 12;
        int y = height / 10;

        g.setFont(new Font("Serif", Font.BOLD, 30));
        g.drawString(heading, margin, y);
        y += 50;

        g.setFont(new Font("Serif", Font.PLAIN, 22));
        FontMetrics metrics = g.getFontMetrics();
        int maxWidth = width - margin * 2;

        for (String paragraph : body.split("\n\n")) {
            for (String line : wrapText(paragraph, metrics, maxWidth)) {
                g.drawString(line, margin, y);
                y += 30;
            }
            y += 16;
        }

        g.setFont(new Font("Serif", Font.PLAIN, 18));
        g.drawString("Page " + (pageIndex + 1), width - margin - 80, height - margin / 2);

        drawPageBorder(g, width, height);
        g.dispose();
        return image;
    }

    private static List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String rawLine : text.split("\n")) {
            StringBuilder current = new StringBuilder();
            for (String word : rawLine.split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (metrics.stringWidth(candidate) > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            lines.add(current.toString());
        }
        return lines;
    }

    private static BufferedImage newPageCanvas(int width, int height) {
        // Grayscale, not RGB -- realistic for a scanned text document and much smaller on disk.
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private static Graphics2D graphicsFor(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        return g;
    }

    private static void drawPageBorder(Graphics2D g, int width, int height) {
        g.setColor(new Color(200, 200, 200));
        g.drawRect(20, 20, width - 40, height - 40);
    }

    private static ImageWriter findTiffWriter() {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No TIFF ImageWriter registered -- is imageio-tiff on the classpath?");
        }
        return writers.next();
    }
}
