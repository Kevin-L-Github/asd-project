package unibs.asd.benchmarks;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import unibs.asd.fileio.BenchmarkReader;

/**
 * Analyzes benchmark matrices, removes empty columns, and outputs results in
 * CSV format.
 */
public class Analyzer {

    private final List<Statistics> benchmarks = new ArrayList<>();
    private int totalFiles = 0;
    private int processedFiles = 0;

    /**
     * Runs benchmark analysis on all files in the directory
     * 
     * @param benchmarkDir Input directory containing benchmark files
     * @param outputPath   Full output path for CSV results (e.g.,
     *                     "results/analysis.csv")
     * @throws IOException If file operations fail
     */
    public void analyzeBenchmarks(String benchmarkDir, String outputPath) throws IOException {
        Path outputFile = prepareOutputFile(outputPath);
        countTotalFiles(benchmarkDir); // Conta prima i file totali
        processBenchmarks(benchmarkDir, outputFile);
        writeCSVResults(outputFile);
        System.out.println("\nCompleted, All benchmarks processed!");
    }

    /**
     * Counts total files to process for progress tracking
     */
    private void countTotalFiles(String benchmarkDir) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(benchmarkDir))) {
            totalFiles = (int) files.filter(Files::isRegularFile).count();
        }
    }

    /**
     * Prepares the output file, creating parent directories if needed
     */
    private Path prepareOutputFile(String outputPath) throws IOException {
        Path file = Paths.get(outputPath);
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);
        return Files.createFile(file);
    }

    /**
     * Processes all benchmark files in the directory
     */
    private void processBenchmarks(String benchmarkDir, Path outputFile) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(benchmarkDir))) {
            files.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(path -> {
                        processBenchmarkFile(path);
                        updateProgress();
                    });
        }
    }

    /**
     * Updates the progress animation with a longer bar
     */
    private void updateProgress() {
        processedFiles++;
        int progress = (int) (((double) processedFiles / totalFiles) * 100);

        // Larghezza della barra di progresso
        final int barLength = 25;
        int filledLength = (int) ((double) processedFiles / totalFiles * barLength);

        // Creazione della barra tipo [=======> ]
        String bar = ">".repeat(Math.max(0, filledLength - 1)) +
                (filledLength > 0 ? ">" : "") +
                " ".repeat(Math.max(0, barLength - filledLength));

        String output = String.format(
                "\rProcessing: %4d/%-4d [%s] %3d%%",
                processedFiles,
                totalFiles,
                bar,
                progress);

        System.out.print(output);
    }

    /**
     * Processes a single benchmark file and collects statistics
     */
    private void processBenchmarkFile(Path inputFile) {
        String filename = inputFile.getFileName().toString();

        boolean[][] originalMatrix = BenchmarkReader.readBenchmark(inputFile.toString());
        boolean[][] processedMatrix = removeEmptyColumns(originalMatrix);

        Statistics stats = new Statistics(
                filename,
                processedMatrix.length,
                processedMatrix[0].length,
                computeSparsity(processedMatrix),
                averageOnesPerRow(processedMatrix),
                maxOnesPerRow(processedMatrix),
                minOnesPerRow(processedMatrix),
                stdDevOnesPerRow(processedMatrix));

        benchmarks.add(stats);
    }

    /**
     * Writes analysis results in CSV format
     * 
     * @param outputFile Path to output CSV file
     * @throws IOException If writing fails
     */
    private void writeCSVResults(Path outputFile) throws IOException {
        // CSV header
        String header = "Filename,Rows,Columns,Sparsity,Avg Ones/Row,Max Ones/Row,Min Ones/Row,StdDev\n";
        Files.writeString(outputFile, header);

        // CSV data rows
        for (Statistics stats : benchmarks) {
            String row = String.format(Locale.US, "%s,%d,%d,%.4f,%.2f,%.2f,%.2f,%.2f%n",
                    stats.filename(),
                    stats.rows(),
                    stats.columns(),
                    stats.sparsity(),
                    stats.avgOnesPerRow(),
                    stats.maxOnesPerRow(),
                    stats.minOnesPerRow(),
                    stats.stdDev());

            Files.writeString(outputFile, row, StandardOpenOption.APPEND);
        }
    }

    // ========== MATRIX PROCESSING METHODS ==========

    /**
     * Removes empty columns from the matrix
     * 
     * @return New matrix with empty columns removed
     */
    private boolean[][] removeEmptyColumns(boolean[][] matrix) {
        if (matrix.length == 0 || matrix[0].length == 0)
            return matrix;

        List<Integer> nonEmptyCols = new ArrayList<>();
        for (int j = 0; j < matrix[0].length; j++) {
            if (!isColumnEmpty(matrix, j)) {
                nonEmptyCols.add(j);
            }
        }

        boolean[][] result = new boolean[matrix.length][nonEmptyCols.size()];
        for (int i = 0; i < matrix.length; i++) {
            for (int k = 0; k < nonEmptyCols.size(); k++) {
                result[i][k] = matrix[i][nonEmptyCols.get(k)];
            }
        }
        return result;
    }

    /**
     * Checks if a column is completely empty
     * 
     * @param matrix   Input matrix
     * @param colIndex Column index to check
     * @return True if column contains no true values
     */
    private boolean isColumnEmpty(boolean[][] matrix, int colIndex) {
        for (boolean[] row : matrix) {
            if (row[colIndex]) {
                return false;
            }
        }
        return true;
    }

    // ========== MATRIX ANALYSIS METHODS ==========

    /**
     * Computes matrix sparsity (percentage of false values)
     * 
     * @param matrix Input matrix
     * @return Sparsity value between 0 and 1
     */
    private float computeSparsity(boolean[][] matrix) {
        int ones = countTotalOnes(matrix);
        int total = matrix.length * matrix[0].length;
        return 1.0f - ((float) ones / total);
    }

    /**
     * Counts total true values in matrix
     * 
     * @param matrix Input matrix
     * @return Total count of true values
     */
    private int countTotalOnes(boolean[][] matrix) {
        int count = 0;
        for (boolean[] row : matrix) {
            for (boolean val : row) {
                if (val)
                    count++;
            }
        }
        return count;
    }

    /**
     * Calculates average true values per row
     * 
     * @param matrix Input matrix
     * @return Average value
     */
    private float averageOnesPerRow(boolean[][] matrix) {
        return (float) countTotalOnes(matrix) / matrix.length;
    }

    /**
     * Finds maximum true values in any row
     * 
     * @param matrix Input matrix
     * @return Maximum count
     */
    private float maxOnesPerRow(boolean[][] matrix) {
        int max = 0;
        for (boolean[] row : matrix) {
            max = Math.max(max, countOnesInRow(row));
        }
        return max;
    }

    /**
     * Finds minimum true values in any row
     * 
     * @param matrix Input matrix
     * @return Minimum count
     */
    private float minOnesPerRow(boolean[][] matrix) {
        int min = Integer.MAX_VALUE;
        for (boolean[] row : matrix) {
            min = Math.min(min, countOnesInRow(row));
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    /**
     * Calculates standard deviation of true values per row
     * 
     * @param matrix Input matrix
     * @return Standard deviation value
     */
    private float stdDevOnesPerRow(boolean[][] matrix) {
        float mean = averageOnesPerRow(matrix);
        float varianceSum = 0;
        for (boolean[] row : matrix) {
            float diff = countOnesInRow(row) - mean;
            varianceSum += diff * diff;
        }
        return (float) Math.sqrt(varianceSum / matrix.length);
    }

    /**
     * Helper method to count true values in a single row
     * 
     * @param row Matrix row
     * @return Count of true values
     */
    private int countOnesInRow(boolean[] row) {
        int count = 0;
        for (boolean val : row) {
            if (val)
                count++;
        }
        return count;
    }
}

/**
 * Record to hold benchmark statistics
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