package unibs.asd.benchmarks;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import unibs.asd.fileio.BenchmarkReader;

public class BenchmarksSelection {

    private static final String SELECTED_FILENAME = "selected_benchmark.txt";
    private final List<BenchmarksStatistics> benchmarks = new ArrayList<>();

    public void runBenchmarks(String benchmarkDir, String destDir) throws IOException {
        Path outputFile = prepareOutputFile(destDir);
        processBenchmarks(benchmarkDir, outputFile);
    }

    private Path prepareOutputFile(String destDir) throws IOException {
        Path dir = Paths.get(destDir);
        if (Files.notExists(dir)) Files.createDirectories(dir);

        Path file = dir.resolve(SELECTED_FILENAME);
        Files.deleteIfExists(file);
        return Files.createFile(file);
    }

    private void processBenchmarks(String benchmarkDir, Path outputFile) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(benchmarkDir))) {
            files.filter(Files::isRegularFile)
                 .sorted(Comparator.comparing(Path::getFileName))
                 .forEach(path -> processBenchmarkFile(path, outputFile));
        }
    }

    private void processBenchmarkFile(Path inputFile, Path outputFile) {
        String filename = inputFile.getFileName().toString();
        System.out.println("[Processing] " + filename);

        boolean[][] instance = BenchmarkReader.readBenchmark(inputFile.toString());

        BenchmarksStatistics stats = new BenchmarksStatistics(
                filename,
                computeSparsity(instance),
                instance.length,
                instance[0].length,
                countEmptyColumns(instance),
                averageOnesPerRow(instance),
                maxOnesPerRow(instance),
                minOnesPerRow(instance),
                stdDevOnesPerRow(instance)
        );

        benchmarks.add(stats);
        appendFilenameToOutput(filename, outputFile);
    }

    private void appendFilenameToOutput(String filename, Path outputFile) {
        try {
            Files.writeString(outputFile, filename + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[Error] Cannot write file: " + filename);
        }
    }

    // === MATRIX ANALYSIS ===

    private float computeSparsity(boolean[][] matrix) {
        int ones = 0;
        int total = matrix.length * matrix[0].length;
        for (boolean[] row : matrix)
            for (boolean val : row)
                if (val) ones++;
        return 1.0f - ((float) ones / total);
    }

    private int countEmptyColumns(boolean[][] matrix) {
        int cols = matrix[0].length;
        int empty = 0;

        for (int j = 0; j < cols; j++) {
            boolean hasOne = false;
            for (boolean[] row : matrix) {
                if (row[j]) {
                    hasOne = true;
                    break;
                }
            }
            if (!hasOne) empty++;
        }
        return empty;
    }

    private float averageOnesPerRow(boolean[][] matrix) {
        int total = 0;
        for (boolean[] row : matrix)
            for (boolean val : row)
                if (val) total++;
        return (float) total / matrix.length;
    }

    private float maxOnesPerRow(boolean[][] matrix) {
        int max = 0;
        for (boolean[] row : matrix) {
            int count = 0;
            for (boolean val : row)
                if (val) count++;
            max = Math.max(max, count);
        }
        return max;
    }

    private float minOnesPerRow(boolean[][] matrix) {
        int min = Integer.MAX_VALUE;
        for (boolean[] row : matrix) {
            int count = 0;
            for (boolean val : row)
                if (val) count++;
            min = Math.min(min, count);
        }
        return min;
    }

    private float stdDevOnesPerRow(boolean[][] matrix) {
        float mean = averageOnesPerRow(matrix);
        float varianceSum = 0;
        for (boolean[] row : matrix) {
            int count = 0;
            for (boolean val : row)
                if (val) count++;
            varianceSum += Math.pow(count - mean, 2);
        }
        return (float) Math.sqrt(varianceSum / matrix.length);
    }
}
