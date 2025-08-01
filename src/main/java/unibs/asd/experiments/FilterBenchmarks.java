package unibs.asd.experiments;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.fileio.BenchmarkWriter;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

/**
 * A class for running and filtering benchmarks for Minimal Hitting Set (MHS)
 * problems.
 * Processes benchmark files sequentially, records solved benchmarks, and
 * handles timeouts.
 * Includes memory management between benchmark executions.
 */
public class FilterBenchmarks {

    // Class attributes for configuration
    private final String benchmarkDir;
    private final String destDir;
    private final String solvedFilename;
    private final int timeoutMs;
    private final Set<String> alreadySolvedBenchmarks;

    /**
     * Constructor for FilterBenchmarks
     * 
     * @param benchmarkDir   Directory containing benchmark files to process
     * @param destDir        Directory where results will be stored
     * @param solvedFilename Name of the file to store solved benchmarks
     * @param timeoutMs      Timeout in milliseconds for each benchmark
     * @param alreadySolvedFiles List of files containing already solved benchmarks (one per line)
     * @throws IOException If there are issues reading the already solved files
     */
    public FilterBenchmarks(String benchmarkDir, String destDir,
            String solvedFilename, int timeoutMs, List<String> alreadySolvedFiles) throws IOException {
        this.benchmarkDir = benchmarkDir;
        this.destDir = destDir;
        this.solvedFilename = solvedFilename;
        this.timeoutMs = timeoutMs;
        this.alreadySolvedBenchmarks = loadAlreadySolvedBenchmarks(alreadySolvedFiles);
    }

    /**
     * Loads already solved benchmarks from the given files
     * 
     * @param filePaths List of file paths containing solved benchmarks
     * @return Set of benchmark names that have already been solved
     * @throws IOException If there are issues reading the files
     */
    private Set<String> loadAlreadySolvedBenchmarks(List<String> filePaths) throws IOException {
        Set<String> solved = new HashSet<>();
        if (filePaths == null || filePaths.isEmpty()) {
            return solved;
        }

        for (String filePath : filePaths) {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                try (Stream<String> lines = Files.lines(path)) {
                    lines.forEach(solved::add);
                }
            }
        }
        return solved;
    }

    /**
     * Main entry point for running benchmarks.
     * 
     * @throws IOException If there are issues with file operations
     */
    public void runBenchmarks() throws IOException {
        Path solvedFile = prepareSolvedFile();
        processBenchmarksSequentially(solvedFile);
    }

    /**
     * Prepares the output file for storing solved benchmarks.
     * 
     * @return Path to the created solved benchmarks file
     * @throws IOException If directory creation or file operations fail
     */
    private Path prepareSolvedFile() throws IOException {
        Path dirPath = Paths.get(destDir);

        // Create destination directory if it doesn't exist
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Prepare a clean output file
        Path solvedFile = dirPath.resolve(solvedFilename);
        Files.deleteIfExists(solvedFile);
        return Files.createFile(solvedFile);
    }

    /**
     * Processes all benchmark files in the specified directory sequentially.
     * 
     * @param solvedFile File to record solved benchmarks
     * @throws IOException If there are issues reading the benchmark files
     */
    private void processBenchmarksSequentially(Path solvedFile) throws IOException {
        try (Stream<Path> filesStream = Files.list(Paths.get(benchmarkDir))) {
            filesStream
                    .filter(Files::isRegularFile) // Only process regular files
                    .filter(path -> !alreadySolvedBenchmarks.contains(path.getFileName().toString())) // Skip already solved
                    .sorted(Comparator.comparing(Path::getFileName)) // Sort files by name
                    .forEach(path -> {
                        clearMemoryBeforeExecution();
                        processSingleBenchmark(path, solvedFile);
                        clearMemoryAfterExecution();
                    });
        }
    }

    /**
     * Processes a single benchmark file, running the MHS algorithm with a timeout.
     * 
     * @param inputFile  Path to the benchmark file to process
     * @param solvedFile File to record successful solutions
     */
    private void processSingleBenchmark(Path inputFile, Path solvedFile) {
        String filename = inputFile.getFileName().toString();
        System.out.println("[Processing] " + filename);

        // Read benchmark data and initialize MHS solver
        boolean[][] instance = BenchmarkReader.readBenchmark(inputFile.toString());
        MHS mhsSolver = new BoostMHS(instance);

        mhsSolver.run(BitSetType.FAST_BITSET, timeoutMs);

        // Check if the benchmark was solved within the timeout period
        if (mhsSolver.isExecuted() && !mhsSolver.isStopped()) {
            writeSolvedBenchmark(filename, solvedFile);
            try {
                BenchmarkWriter.writeBenchmark(mhsSolver, filename, destDir);
            } catch (IOException e) {
                System.err.println("[Error] Failed to write benchmark solution for " + filename);
                e.printStackTrace();
            }
            long executionTime = (long) mhsSolver.getComputationTime();
            System.out.println("[Solved] " + filename + " in " + executionTime + "ms");
        } else {
            System.out.println("[Timeout] " + filename);
        }
    }

    /**
     * Records a successfully solved benchmark in the output file.
     * 
     * @param filename   Name of the solved benchmark file
     * @param solvedFile File to append the solution record to
     */
    private void writeSolvedBenchmark(String filename, Path solvedFile) {
        try {
            Files.writeString(solvedFile, filename + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[Error] Failed to write " + filename + " to solved benchmarks file");
        }
    }

    /**
     * Attempts to clear memory before executing a benchmark.
     * Helps prevent interference between benchmark executions.
     */
    private void clearMemoryBeforeExecution() {
        System.gc(); // Suggest garbage collection
        try {
            Thread.sleep(100); // Short pause to allow GC to work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Attempts to clear memory after executing a benchmark.
     * Helps prevent memory buildup during sequential processing.
     */
    private void clearMemoryAfterExecution() {
        System.gc(); // Suggest garbage collection
        try {
            Thread.sleep(100); // Short pause to allow GC to work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}