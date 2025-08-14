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
 * Utility class for writing benchmark results to files in a structured,
 * parsable format.
 * Generates detailed reports about MHS algorithm execution with clear section
 * headers
 * and key-value pairs for easy parsing, with all comments prefixed by ;;;.
 */
public class BenchmarkWriter {

    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.###");
    private static final String COMMENT_PREFIX = ";;; ";
    private static final String SECTION_PREFIX = ";;; === ";
    private static final String SECTION_SUFFIX = " ===\n";
    private static final String SOLUTIONS_SECTION_HEADER = "=== SOLUTIONS ===\n";

    /**
     * Writes benchmark results to a file in a structured, parsable format.
     * All descriptive lines are prefixed with ;;; for easy identification.
     */
    public static void writeBenchmark(MHS mhs, String filename, String destDir) throws IOException {
        validateExecutionStatus(mhs);

        String outputFilename = getOutputFilename(filename);
        Path outputPath = Paths.get(destDir, outputFilename);

        BenchmarkStatistics stats = collectStatistics(mhs);

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writeExecutionSummary(writer, mhs, stats);
            writeMatrixInfo(writer, stats);
            writePerformanceInfo(writer, mhs);
            writeSolutionsSection(writer, stats.solutions);
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
        stats.DEPTH = mhs.getDEPTH();
        stats.depthLimit = mhs.getDepthLimit();

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

    private static void writeExecutionSummary(FileWriter writer, MHS mhs, BenchmarkStatistics stats)
            throws IOException {
        writer.write(SECTION_PREFIX + "EXECUTION SUMMARY" + SECTION_SUFFIX);

        writer.write(COMMENT_PREFIX + "Algorithm completion status: " + getExecutionStatus(mhs) + "\n");
        writer.write(COMMENT_PREFIX + "Number of solutions found: " + stats.solutions.size() + "\n");
        writer.write(COMMENT_PREFIX + "Min cardinality: " + stats.minCardinality + "\n");
        writer.write(COMMENT_PREFIX + "Max cardinality: " + stats.maxCardinality + "\n");
        writer.write(COMMENT_PREFIX + "Maximum search depth: " + stats.depthLimit + "\n");
        writer.write(COMMENT_PREFIX + "Depth reached: " + stats.DEPTH + "/" + stats.depthLimit + "\n");

        if (mhs.isStopped()) {
            writer.write(COMMENT_PREFIX + "Execution was interrupted, reason: " + getStopReason(mhs) + "\n");
        }

        writer.write("\n");
    }

    private static void writeMatrixInfo(FileWriter writer, BenchmarkStatistics stats) throws IOException {
        writer.write(SECTION_PREFIX + "MATRIX INFORMATION" + SECTION_SUFFIX);

        writer.write(COMMENT_PREFIX + "Number of rows (elements): " + stats.instance.length + "\n");
        writer.write(COMMENT_PREFIX + "Total number of columns: " + stats.instance[0].length + "\n");
        writer.write(COMMENT_PREFIX + "Number of non-empty columns: " + stats.nonEmptyColumns + "\n");
        writer.write(COMMENT_PREFIX + "Number of empty columns: " + stats.emptyColumns + "\n");

        writer.write("\n");
    }

    private static void writePerformanceInfo(FileWriter writer, MHS mhs) throws IOException {
        writer.write(SECTION_PREFIX + "PERFORMANCE METRICS" + SECTION_SUFFIX);

        writer.write(COMMENT_PREFIX + "Total computation time (nanoseconds): " + mhs.getComputationTime() + "\n");
        writer.write(COMMENT_PREFIX + "Human-readable computation time: " + formatDetailedTime(mhs.getComputationTime())
                + "\n");

        writer.write("\n");
    }

    private static void writeSolutionsSection(FileWriter writer, List<Hypothesis> solutions) throws IOException {
        writer.write(SOLUTIONS_SECTION_HEADER);
        writer.write("\n");
        if (solutions.isEmpty()) {
            writer.write("No solutions found.\n");
        } else {
            for (Hypothesis solution : solutions) {
                writer.write(solution.toString().trim() + "\n");
            }
        }
    }

    private static String getExecutionStatus(MHS mhs) {
        if (!mhs.isExecuted())
            return "NOT EXECUTED";
        if (mhs.isStopped())
            return "STOPPED BEFORE COMPLETION";
        return "COMPLETED SUCCESSFULLY";
    }

    private static String getStopReason(MHS mhs) {
        if (mhs.isOutOfMemoryError())
            return "OUT OF MEMORY";
        if (mhs.isStoppedInsideLoop())
            return "TIMEOUT DURING PROCESSING";
        return "TIMEOUT BEFORE PROCESSING";
    }

    private static String formatDetailedTime(double timeNs) {
        if (timeNs < 1_000) {
            return TIME_FORMAT.format(timeNs) + " nanoseconds";
        }

        if (timeNs < 1_000_000) {
            return TIME_FORMAT.format(timeNs / 1_000) + " microseconds";
        }

        if (timeNs < 1_000_000_000) {
            return TIME_FORMAT.format(timeNs / 1_000_000) + " milliseconds";
        }

        double seconds = timeNs / 1_000_000_000;
        if (seconds >= 60) {
            long minutes = TimeUnit.NANOSECONDS.toMinutes((long) timeNs);
            long remainingSeconds = TimeUnit.NANOSECONDS.toSeconds((long) timeNs) -
                    TimeUnit.MINUTES.toSeconds(minutes);
            return TIME_FORMAT.format(seconds) + " seconds (" + minutes + " minutes " + remainingSeconds + " seconds)";
        }

        return TIME_FORMAT.format(seconds) + " seconds";
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
        int DEPTH;
        int depthLimit;
    }
}