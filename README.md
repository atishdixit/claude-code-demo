# tiff-to-pdf-converter

A small, self-contained Java/Maven utility: converts every `.tif`/`.tiff` file in
`input/` into a PDF in `output/`, using [iText](https://itextpdf.com) (core, AGPL).
Multi-page TIFFs become multi-page PDFs, one PDF page per TIFF frame, each sized to
match its source image exactly.

See [ARCHITECTURE.md](ARCHITECTURE.md) for how it's put together and
[SETUP.md](SETUP.md) for prerequisites and build steps.

## Quick start

```bash
mvn clean package
run.bat
```

That's it — `run.bat` runs the jar against whatever is in `input/` right now. The
repo ships with 4 sample files in `input/` already (see [Test files](#test-files)
below), so this works immediately after a clone with no setup beyond having a JDK
and Maven installed.

## Folder layout (fixed, not configurable via flags)

```
tiff-to-pdf-converter/
├── input/     <- drop .tif / .tiff files here
├── output/    <- matching .pdf files land here (same base filename)
└── logs/
    ├── health.log   <- every execution log line (INFO and above)
    └── error.log    <- ERROR-level entries only, with full stack traces
```

All three are relative to wherever you run the jar from — `run.bat` `cd`s into the
project root first, so this "just works" regardless of where you launch it from.

## Behavior / exception handling

- **One bad file never stops the batch.** Each file is converted independently;
  a corrupt or unreadable TIFF is logged to `error.log` and skipped, and every
  other file still gets processed.
- **A failed conversion never leaves a partial/corrupt PDF behind** — if anything
  goes wrong partway through writing a PDF, that output file is deleted.
- **Missing `input/` folder** — created automatically, run ends cleanly (nothing
  to convert yet).
- **Empty `input/` folder** — logged as a warning, run ends cleanly.
- **Missing `output/`/`logs/` folders** — created automatically.
- **Non-`.tif`/`.tiff` files in `input/`** — ignored entirely (not even attempted).
- **Exit codes**: `0` = everything converted (or nothing to do), `1` = one or more
  files failed to convert, `2` = the run couldn't start at all (e.g. folders
  couldn't be created due to a permissions problem).

## Test files

`input/` ships with 4 checked-in sample files so the behavior above is visible on
first run without you needing to supply anything:

| File | Purpose |
|---|---|
| `sample-single-page.tiff` | normal case: 1-page TIFF → 1-page PDF |
| `sample-multi-page.tiff` | 3-page TIFF → 3-page PDF |
| `sample-corrupt.tiff` | not a real TIFF (plain text with a `.tiff` extension) — demonstrates a single bad file being logged to `error.log` and skipped, without affecting the other two |
| `sample-not-a-tiff.txt` | wrong extension entirely — demonstrates it's ignored, not counted, not attempted |

Running `run.bat` against these as-is produces exit code `1` (because of the
deliberately-corrupt file) with 2 PDFs in `output/` and one error entry in
`logs/error.log` — this is the expected, correct result, not a bug.

These same files are also generated programmatically by
[`TiffFixtureGenerator`](src/test/java/com/example/tiffconverter/util/TiffFixtureGenerator.java)
and used directly in the automated test suite (see below), so the checked-in
copies and the test fixtures are guaranteed to describe the same scenarios.

## Automated tests

```bash
mvn test
```

12 JUnit 5 tests across 3 classes, all using real generated TIFF files (via
`TiffFixtureGenerator`, backed by the TwelveMonkeys ImageIO TIFF plugin — test
scope only, not a runtime dependency):

- `TiffToPdfConverterTest` — single-page conversion, multi-page page-count
  preservation, corrupt file throws + leaves no partial output, missing file
  throws, empty file throws.
- `FileScannerTest` — finds `.tif`/`.tiff` case-insensitively, ignores other
  extensions, ignores empty directories, ignores directories named like a TIFF.
- `BatchConverterTest` — mixed valid/corrupt/ignored files in one batch (asserts
  exact succeeded/failed counts and which output files exist), missing input
  directory auto-created, empty input directory, output directory auto-created
  when nested/missing.

## Licensing note

iText core is used here under **AGPL** (the free tier) via Maven Central — fine
for this demo/internal-tool use. If you build on this for a closed-source product
you distribute or offer as a service, iText's AGPL terms require your own source
to be open too, unless you buy an iText commercial license.
