package unibs.asd.fileio;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.MHS;

/**
 * Utility class for writing benchmark results to files.
 * Generates detailed reports about MHS algorithm execution including:
 * - Solutions found
 * - Performance metrics
 * - Execution statistics
 * - Algorithm status information
 */
public class BenchmarkWriter {

    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.###");
    private static final String COMMENT_PREFIX = ";;; ";

    /**
     * Writes benchmark results to a file with comprehensive execution details.
     * 
     * @param mhs      The MHS algorithm instance containing results
     * @param filename Original input filename (will be modified to .mhs extension)
     * @param destDir  Destination directory for output file
     * @throws IOException              If file writing fails
     * @throws IllegalArgumentException If algorithm hasn't been executed
     */
    public static void writeBenchmark(MHS mhs, String filename, String destDir) throws IOException {
        validateExecutionStatus(mhs);

        String outputFilename = getOutputFilename(filename);
        Path outputPath = Paths.get(destDir, outputFilename);

        BenchmarkStatistics stats = collectStatistics(mhs);

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writeSolutions(writer, stats.solutions);
            writeExecutionReport(writer, mhs, stats);
        }
    }

    private static void validateExecutionStatus(MHS mhs) {
        if (!mhs.isExecuted()) {
            throw new IllegalArgumentException("Algorithm has not been executed!");
        }
    }

    private static String getOutputFilename(String filename) {
        return filename.replaceAll("\\.matrix$", ".mhs");
    }

    private static BenchmarkStatistics collectStatistics(MHS mhs) {
        BenchmarkStatistics stats = new BenchmarkStatistics();
        stats.solutions = mhs.getSolutions();
        stats.instance = mhs.getInstance();
        stats.nonEmptyColumns = mhs.getNonEmptyColumns().size();
        stats.emptyColumns = stats.instance[0].length - stats.nonEmptyColumns;

        stats.minCardinality = stats.solutions.stream()
                .mapToInt(Hypothesis::cardinality)
                .min()
                .orElse(0);

        stats.maxCardinality = stats.solutions.stream()
                .mapToInt(Hypothesis::cardinality)
                .max()
                .orElse(0);

        return stats;
    }

    /**
     * Writes the list of solutions to the output file in a clear, readable format.
     * Each solution is written on its own line with consistent formatting.
     * 
     * @param writer    The FileWriter instance used to write the output
     * @param solutions List of Hypothesis solutions to be written (may be empty)
     * @throws IOException if an I/O error occurs during writing
     */
    private static void writeSolutions(FileWriter writer, List<Hypothesis> solutions) throws IOException {
        // Write section header
        writer.write("=== SOLUTIONS ===\n\n");

        if (solutions.isEmpty()) {
            writer.write("No solutions found.\n");
        } else {
            // Write each solution with consistent spacing
            for (Hypothesis solution : solutions) {
                writer.write(solution.toString().trim() + "\n");
            }
        }

        // Add section footer
        writer.write("\n=================\n\n");
    }

    private static void writeExecutionReport(FileWriter writer, MHS mhs, BenchmarkStatistics stats) throws IOException {
        writer.write(COMMENT_PREFIX + "Execution Report\n");
        writer.write(COMMENT_PREFIX + "================\n");

        // Basic information
        writer.write(COMMENT_PREFIX + "Number of solutions: " + stats.solutions.size() + "\n");
        writer.write(COMMENT_PREFIX + "Min Cardinality: " + stats.minCardinality + "\n");
        writer.write(COMMENT_PREFIX + "Max Cardinality: " + stats.maxCardinality + "\n");

        // Matrix information
        writer.write(COMMENT_PREFIX + "Matrix dimensions (N x M): " +
                stats.instance.length + " x " + stats.instance[0].length + "\n");
        writer.write(COMMENT_PREFIX + "Non-empty columns: " + stats.nonEmptyColumns + "\n");
        writer.write(COMMENT_PREFIX + "Empty columns: " + stats.emptyColumns + "\n");

        // Performance metrics
        writer.write(COMMENT_PREFIX + "Computation time: " + formatDetailedTime(mhs.getComputationTime()) + "\n");

        // Execution status
        writer.write(COMMENT_PREFIX + "Execution status: " + getExecutionStatus(mhs) + "\n");
        if (mhs.isStopped()) {
            writer.write(COMMENT_PREFIX + "Stopped reason: " + getStopReason(mhs) + "\n");
        }
        writer.write(COMMENT_PREFIX + "Search depth: " + mhs.getDEPTH() + "\n");
    }

    private static String getExecutionStatus(MHS mhs) {
        if (!mhs.isExecuted())
            return "Not executed";
        if (mhs.isStopped())
            return "Stopped before completion";
        return "Completed successfully";
    }

    private static String getStopReason(MHS mhs) {
        if (mhs.isOutOfMemoryError())
            return "Out of memory";
        if (mhs.isStoppedInsideLoop())
            return "Timeout during processing";
        return "Timeout before processing";
    }

    /**
     * Formats time with appropriate units and full breakdown.
     * Example output: "1.234 s (1234 ms, 1234000 µs, 1234000000 ns)"
     */
    private static String formatDetailedTime(double timeNs) {
        if (timeNs < 1_000) {
            return TIME_FORMAT.format(timeNs) + " ns";
        }

        StringBuilder sb = new StringBuilder();

        // Main unit
        if (timeNs < 1_000_000) {
            sb.append(TIME_FORMAT.format(timeNs / 1_000)).append(" µs");
        } else if (timeNs < 1_000_000_000) {
            sb.append(TIME_FORMAT.format(timeNs / 1_000_000)).append(" ms");
        } else {
            double seconds = timeNs / 1_000_000_000;
            sb.append(TIME_FORMAT.format(seconds)).append(" s");

            // Add human-readable format for times > 1 second
            if (seconds >= 60) {
                long minutes = TimeUnit.NANOSECONDS.toMinutes((long) timeNs);
                long remainingSeconds = TimeUnit.NANOSECONDS.toSeconds((long) timeNs) -
                        TimeUnit.MINUTES.toSeconds(minutes);
                sb.append(" (").append(minutes).append("m ").append(remainingSeconds).append("s)");
            }
        }

        // Add full breakdown in parentheses
        sb.append(" (")
                .append(TIME_FORMAT.format(timeNs / 1_000)).append(" µs, ")
                .append(TIME_FORMAT.format(timeNs)).append(" ns)");

        return sb.toString();
    }

    /**
     * Helper class to hold collected statistics about the benchmark run
     */
    private static class BenchmarkStatistics {
        List<Hypothesis> solutions;
        boolean[][] instance;
        int nonEmptyColumns;
        int emptyColumns;
        int minCardinality;
        int maxCardinality;
    }
}