package com.example.tiffconverter.service;

/** Outcome of a batch run, used to decide the process exit code. */
public record ConversionSummary(int found, int succeeded, int failed) {

    public boolean hasFailures() {
        return failed > 0;
    }
}
