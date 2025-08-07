package unibs.asd.experiments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.fileio.BenchmarkWriter;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

/**
 * The Experiment class provides functionality to run benchmarks for finding
 * minimal hitting sets.
 * It processes benchmark files, runs the MHS algorithm with a timeout, and
 * writes the results.
 */
public class Experiment {

    private static final String BENCHMARKS_DIRECTORY = "src/mybenchmarks";
    private static final long TIMEOUT_MS = 180_000; // three minutes
    private static final DecimalFormat PROGRESS_FORMAT = new DecimalFormat("0.00%");
    private static final int PROGRESS_BAR_LENGTH = 25;

    /**
     * Runs benchmarks specified in the input file and writes results to the
     * destination directory.
     * 
     * @param filename The path to the file containing list of benchmark files to
     *                 process
     * @param destDir  The directory where results will be written
     * @throws IOException If there are problems reading input files or writing
     *                     results
     */
    public static void runBenchmarks(String filename, String destDir) throws IOException {
        printSectionHeader("BENCHMARK PROCESSING TOOL");
        System.out.printf("%-20s: %s%n", "Input file", filename);
        System.out.printf("%-20s: %s%n", "Output directory", destDir);
        System.out.printf("%-20s: %d ms%n%n", "Timeout", TIMEOUT_MS);

        // Create output directory if it doesn't exist
        Files.createDirectories(Paths.get(destDir));
        List<String> benchmarkFiles = Files.readAllLines(Paths.get(filename));
        int totalBenchmarks = benchmarkFiles.size();
        Instant startTime = Instant.now();

        System.out.println("Starting processing of " + totalBenchmarks + " benchmarks...\n");

        int successCount = 0;
        int currentIndex = 0;

        // Process each benchmark file
        for (String benchmarkFileName : benchmarkFiles) {
            currentIndex++;
            benchmarkFileName = benchmarkFileName.trim();

            printProgress(currentIndex, totalBenchmarks, benchmarkFileName);

            Path filePath = Paths.get(BENCHMARKS_DIRECTORY, benchmarkFileName);
            if (processBenchmarkFile(filePath, destDir, benchmarkFileName)) {
                successCount++;
            }

            // Attempt to free memory between benchmarks
            System.gc();
            try {
                // Small delay to allow system resources to stabilize
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.err.println("Processing interrupted: " + e.getMessage());
            }
        }

        printSummary(startTime, totalBenchmarks, successCount);
    }

    /**
     * Prints a progress bar showing current processing status.
     * 
     * @param current  The current benchmark index being processed
     * @param total    The total number of benchmarks to process
     * @param filename The name of the current benchmark file
     */
    private static void printProgress(int current, int total, String filename) {
        double progress = (double) current / total;
        int filledLength = (int) (PROGRESS_BAR_LENGTH * progress);

        String progressBar = "[" + "=".repeat(filledLength) +
                " ".repeat(PROGRESS_BAR_LENGTH - filledLength) + "]";

        System.out.printf("\r%s %s - %-25s",
                progressBar,
                PROGRESS_FORMAT.format(progress),
                filename.length() > 50 ? filename.substring(0, 47) + "..." : filename);
    }

    /**
     * Processes a single benchmark file by reading it, running MHS algorithm, and
     * writing results.
     * 
     * @param filePath         Path to the benchmark file to process
     * @param destDir          Directory where results will be written
     * @param originalFileName Original name of the benchmark file
     * @return true if processing was successful, false otherwise
     */
    private static boolean processBenchmarkFile(Path filePath, String destDir, String originalFileName) {
        String fileName = filePath.getFileName().toString();
        try {
            // Read benchmark instance and run MHS algorithm
            boolean[][] instance = BenchmarkReader.readBenchmark(filePath.toString());
            MHS mhs = new BoostMHS(instance);
            mhs.run(BitSetType.FAST_BITSET, TIMEOUT_MS);

            // Write results and clean up
            BenchmarkWriter.writeBenchmark(mhs, originalFileName, destDir);
            mhs = null; // Clear reference to allow garbage collection
            return true;
        } catch (Exception e) {
            System.err.printf("%n[ERROR] %s: %s%n", fileName, e.getMessage());
            return false;
        }
    }

    /**
     * Prints a summary of the benchmark execution including statistics and timing.
     * 
     * @param startTime       The time when benchmarking started
     * @param totalBenchmarks Total number of benchmarks processed
     * @param successCount    Number of benchmarks successfully processed
     */
    private static void printSummary(Instant startTime, int totalBenchmarks, int successCount) {
        Duration duration = Duration.between(startTime, Instant.now());
        String formattedDuration = String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());

        printSectionHeader("EXECUTION SUMMARY");
        System.out.printf("%-20s: %d%n", "Total benchmarks", totalBenchmarks);
        System.out.printf("%-20s: %d%n", "Successful", successCount);
        System.out.printf("%-20s: %d%n", "Failed", totalBenchmarks - successCount);
        System.out.printf("%-20s: %s%n%n", "Total time", formattedDuration);
    }

    /**
     * Prints a formatted section header for better output organization.
     * 
     * @param title The title of the section to display
     */
    private static void printSectionHeader(String title) {
        System.out.println("\n" + title);
        System.out.println("=".repeat(title.length()) + "\n");
    }
}