package unibs.asd.benchmarks;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import unibs.asd.fileio.BenchmarkReader;

/**
 * Analyzes benchmark matrices, removes empty columns, and outputs results in
 * CSV format.
 * The analyzer processes boolean matrices, computes various statistics, and
 * generates
 * a comprehensive report about matrix characteristics.
 */
public class Analyzer {

    // List to store statistics for all processed benchmarks
    private final List<Statistics> benchmarksStatistics = new ArrayList<>();

    // Progress tracking variables
    private int totalFilesToProcess = 0;
    private int processedFilesCount = 0;

    /**
     * Main method to analyze benchmarks in a directory and write results to CSV
     * 
     * @param benchmarkDirectory Path to directory containing benchmark files
     * @param outputFilePath     Full path for output CSV file (e.g.,
     *                           "results/analysis.csv")
     * @throws IOException If file operations fail
     */
    public void analyzeBenchmarks(String benchmarkDirectory, String outputFilePath) throws IOException {
        Path outputFile = prepareOutputFile(outputFilePath);
        countTotalBenchmarkFiles(benchmarkDirectory);
        processAllBenchmarks(benchmarkDirectory, outputFile);
        writeStatisticsToCSV(outputFile);
        System.out.println("\nAnalysis completed! Processed " + processedFilesCount + " benchmark files.");
    }

    /**
     * Counts total benchmark files in directory for progress tracking
     * 
     * @param benchmarkDirectory Path to directory containing benchmark files
     * @throws IOException If directory access fails
     */
    private void countTotalBenchmarkFiles(String benchmarkDirectory) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(benchmarkDirectory))) {
            totalFilesToProcess = (int) files.filter(Files::isRegularFile).count();
        }
    }

    /**
     * Prepares the output file by creating necessary directories
     * 
     * @param outputPath Desired output file path
     * @return Path object for the output file
     * @throws IOException If file creation fails
     */
    private Path prepareOutputFile(String outputPath) throws IOException {
        Path outputFile = Paths.get(outputPath);
        Files.createDirectories(outputFile.getParent());
        Files.deleteIfExists(outputFile);
        return Files.createFile(outputFile);
    }

    /**
     * Processes all benchmark files in the directory
     * 
     * @param benchmarkDirectory Directory containing benchmark files
     * @param outputFile         Path to output file for results
     * @throws IOException If file processing fails
     */
    private void processAllBenchmarks(String benchmarkDirectory, Path outputFile) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(benchmarkDirectory))) {
            files.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(filePath -> {
                        processSingleBenchmarkFile(filePath);
                        updateProgressIndicator();
                    });
        }
    }

    /**
     * Updates the progress indicator with a visual progress bar
     */
    private void updateProgressIndicator() {
        processedFilesCount++;
        int progressPercentage = (int) (((double) processedFilesCount / totalFilesToProcess) * 100);

        // Progress bar visualization parameters
        final int progressBarWidth = 25;
        int filledSegments = (int) ((double) processedFilesCount / totalFilesToProcess * progressBarWidth);

        // Create progress bar string: [=====> ]
        String progressBar = "=".repeat(Math.max(0, filledSegments - 1)) +
                (filledSegments > 0 ? ">" : "") +
                " ".repeat(Math.max(0, progressBarWidth - filledSegments));

        String progressOutput = String.format(
                "\rProcessing: %4d/%-4d [%s] %3d%%",
                processedFilesCount,
                totalFilesToProcess,
                progressBar,
                progressPercentage);

        System.out.print(progressOutput);
    }

    /**
     * Processes a single benchmark file and collects its statistics
     * 
     * @param benchmarkFile Path to the benchmark file to process
     */
    private void processSingleBenchmarkFile(Path benchmarkFile) {
        String fileName = benchmarkFile.getFileName().toString();

        // Read and preprocess the matrix
        boolean[][] originalMatrix = BenchmarkReader.readBenchmark(benchmarkFile.toString());
        boolean[][] processedMatrix = removeEmptyColumns(originalMatrix);

        // Calculate all statistics for this benchmark
        Statistics stats = new Statistics(
                fileName,
                processedMatrix.length, // row count
                processedMatrix[0].length, // column count
                calculateMatrixSparsity(processedMatrix), // sparsity ratio
                calculateAverageOnesPerRow(processedMatrix), // mean ones per row
                findMaxOnesInAnyRow(processedMatrix), // max ones in a row
                findMinOnesInAnyRow(processedMatrix), // min ones in a row
                calculateStdDevOfOnesPerRow(processedMatrix) // standard deviation
        );

        benchmarksStatistics.add(stats);
    }

    /**
     * Writes collected statistics to CSV file
     * 
     * @param outputFile Path to output CSV file
     * @throws IOException If writing fails
     */
    private void writeStatisticsToCSV(Path outputFile) throws IOException {
        // Write CSV header
        String header = "Filename,Rows,Columns,Sparsity,Avg Ones/Row,Max Ones/Row,Min Ones/Row,StdDev\n";
        Files.writeString(outputFile, header);

        // Write each benchmark's statistics as a CSV row
        for (Statistics stats : benchmarksStatistics) {
            String csvRow = String.format(Locale.US, "%s,%d,%d,%.4f,%.2f,%.2f,%.2f,%.2f%n",
                    stats.filename(),
                    stats.rows(),
                    stats.columns(),
                    stats.sparsity(),
                    stats.avgOnesPerRow(),
                    stats.maxOnesPerRow(),
                    stats.minOnesPerRow(),
                    stats.stdDev());

            Files.writeString(outputFile, csvRow, StandardOpenOption.APPEND);
        }
    }

    // ========== MATRIX PROCESSING METHODS ==========

    /**
     * Removes empty columns (columns with all false values) from the matrix
     * 
     * @param inputMatrix Original boolean matrix to process
     * @return New matrix with empty columns removed
     */
    private boolean[][] removeEmptyColumns(boolean[][] inputMatrix) {
        if (inputMatrix.length == 0 || inputMatrix[0].length == 0) {
            return inputMatrix;
        }

        // Identify indices of non-empty columns
        List<Integer> columnsToKeep = new ArrayList<>();
        for (int colIndex = 0; colIndex < inputMatrix[0].length; colIndex++) {
            if (!isColumnEmpty(inputMatrix, colIndex)) {
                columnsToKeep.add(colIndex);
            }
        }

        // Create new matrix with only non-empty columns
        boolean[][] filteredMatrix = new boolean[inputMatrix.length][columnsToKeep.size()];
        for (int rowIndex = 0; rowIndex < inputMatrix.length; rowIndex++) {
            for (int newColIndex = 0; newColIndex < columnsToKeep.size(); newColIndex++) {
                int originalColIndex = columnsToKeep.get(newColIndex);
                filteredMatrix[rowIndex][newColIndex] = inputMatrix[rowIndex][originalColIndex];
            }
        }

        return filteredMatrix;
    }

    /**
     * Checks if a matrix column contains only false values
     * 
     * @param matrix      The matrix to check
     * @param columnIndex Index of the column to examine
     * @return True if the column is empty (all false), false otherwise
     */
    private boolean isColumnEmpty(boolean[][] matrix, int columnIndex) {
        for (boolean[] row : matrix) {
            if (row[columnIndex]) {
                return false;
            }
        }
        return true;
    }

    // ========== MATRIX STATISTICS CALCULATION METHODS ==========

    /**
     * Computes the sparsity ratio of the matrix (percentage of false values)
     * 
     * @param matrix The matrix to analyze
     * @return Sparsity value between 0.0 (no false values) and 1.0 (all false)
     */
    private float calculateMatrixSparsity(boolean[][] matrix) {
        int trueValuesCount = countTrueValuesInMatrix(matrix);
        int totalCells = matrix.length * matrix[0].length;
        return 1.0f - ((float) trueValuesCount / totalCells);
    }

    /**
     * Counts all true values in the entire matrix
     * 
     * @param matrix The matrix to analyze
     * @return Total count of true values
     */
    private int countTrueValuesInMatrix(boolean[][] matrix) {
        int count = 0;
        for (boolean[] row : matrix) {
            for (boolean value : row) {
                if (value) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Calculates the average number of true values per row
     * 
     * @param matrix The matrix to analyze
     * @return Average number of true values per row
     */
    private float calculateAverageOnesPerRow(boolean[][] matrix) {
        return (float) countTrueValuesInMatrix(matrix) / matrix.length;
    }

    /**
     * Finds the maximum number of true values in any single row
     * 
     * @param matrix The matrix to analyze
     * @return Maximum count of true values found in any row
     */
    private float findMaxOnesInAnyRow(boolean[][] matrix) {
        int maxCount = 0;
        for (boolean[] row : matrix) {
            maxCount = Math.max(maxCount, countTrueValuesInRow(row));
        }
        return maxCount;
    }

    /**
     * Finds the minimum number of true values in any single row
     * 
     * @param matrix The matrix to analyze
     * @return Minimum count of true values found in any row
     */
    private float findMinOnesInAnyRow(boolean[][] matrix) {
        int minCount = Integer.MAX_VALUE;
        for (boolean[] row : matrix) {
            minCount = Math.min(minCount, countTrueValuesInRow(row));
        }
        return minCount == Integer.MAX_VALUE ? 0 : minCount;
    }

    /**
     * Calculates the standard deviation of true values counts per row
     * 
     * @param matrix The matrix to analyze
     * @return Standard deviation of true values counts across rows
     */
    private float calculateStdDevOfOnesPerRow(boolean[][] matrix) {
        float mean = calculateAverageOnesPerRow(matrix);
        float sumOfSquaredDifferences = 0;

        for (boolean[] row : matrix) {
            float difference = countTrueValuesInRow(row) - mean;
            sumOfSquaredDifferences += difference * difference;
        }

        return (float) Math.sqrt(sumOfSquaredDifferences / matrix.length);
    }

    /**
     * Counts true values in a single matrix row
     * 
     * @param row A single row from the matrix
     * @return Count of true values in the row
     */
    private int countTrueValuesInRow(boolean[] row) {
        int count = 0;
        for (boolean value : row) {
            if (value) {
                count++;
            }
        }
        return count;
    }
}

/**
 * Immutable record to hold benchmark statistics
 * 
 * @param filename      Name of the benchmark file
 * @param rows          Number of rows in the processed matrix
 * @param columns       Number of columns in the processed matrix
 * @param sparsity      Sparsity ratio (0-1) of the matrix
 * @param avgOnesPerRow Average true values per row
 * @param maxOnesPerRow Maximum true values in any row
 * @param minOnesPerRow Minimum true values in any row
 * @param stdDev        Standard deviation of true values per row
 */
record Statistics(
        String filename,
        int rows,
        int columns,
        float sparsity,
        float avgOnesPerRow,
        float maxOnesPerRow,
        float minOnesPerRow,
        float stdDev) {
}