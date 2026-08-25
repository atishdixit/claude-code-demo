package com.example.tiffconverter.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

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
                writer.writeToSequence(new IIOImage(page, null, null), param);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
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

    private static ImageWriter findTiffWriter() {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No TIFF ImageWriter registered -- is imageio-tiff on the classpath?");
        }
        return writers.next();
    }
}
