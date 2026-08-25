package com.example.tiffconverter.exception;

/**
 * Raised when a single TIFF file fails to convert. Kept as a checked
 * exception so callers can't accidentally forget to handle a per-file
 * failure -- the batch runner catches this per file and keeps going.
 */
public class TiffConversionException extends Exception {

    public TiffConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TiffConversionException(String message) {
        super(message);
    }
}
