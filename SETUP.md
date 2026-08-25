# Setup

## Prerequisites

- **Java 21+ JDK** (not just a JRE — Maven needs `javac`)
- **Maven 3.9+**

Verify both are installed and on `PATH`:

```bash
java -version
mvn -version
```

If either is missing on Windows and you have `winget` available:

```bash
winget install EclipseAdoptium.Temurin.21.JDK
winget install Apache.Maven
```

(Close and reopen your terminal afterwards so the updated `PATH` takes effect.)

## Get the code

```bash
git clone https://github.com/atishdixit/claude-code-demo.git
git checkout tiff-to-pdf-converter
cd tiff-to-pdf-converter
```

## Build

```bash
mvn clean package
```

This compiles the app, runs the test suite (`mvn test` runs automatically as part
of `package`), and produces a single runnable "fat jar" at
`target/tiff-to-pdf-converter-1.0.0.jar` with all dependencies bundled in.

To build without running tests:

```bash
mvn clean package -DskipTests
```

To run just the tests:

```bash
mvn test
```

## Run

**Windows (recommended):**

```bat
run.bat
```

**Any OS, directly:**

```bash
java -jar target/tiff-to-pdf-converter-1.0.0.jar
```

Either way, it converts everything currently in `input/` (the repo ships with 4
sample files there already — see the README's [Test files](README.md#test-files)
section) and writes results to `output/` and `logs/`.

## Using it with your own files

1. Delete or keep the sample files in `input/` as you like — anything with a
   `.tif`/`.tiff` extension in there gets processed.
2. Copy your own `.tif`/`.tiff` files into `input/`.
3. Run `run.bat` (or the `java -jar ...` command above).
4. Matching `.pdf` files appear in `output/`; check `logs/health.log` for a full
   run log, or `logs/error.log` if the exit code was `1` (some files failed).

## Regenerating the sample test files

The 4 sample files in `input/` are generated, not hand-crafted. To regenerate
them (e.g. after changing `TiffFixtureGenerator`):

```bash
mvn -q test-compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
    -Dexec.mainClass=com.example.tiffconverter.util.GenerateInputFixtures \
    -Dexec.classpathScope=test
```
