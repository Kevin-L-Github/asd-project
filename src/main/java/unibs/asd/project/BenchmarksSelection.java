package unibs.asd.project;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class BenchmarksSelection {

    private static String SELECTED_FILENAME = "selected_benchmark.txt";
    private List<BenchmarksStatistics> benchmarks = new ArrayList<>();

    public void runBenchmarks(String benchmarkDir, String destDir) throws IOException {
        Path solvedFile = prepareSelectedFile(destDir);
        processBenchmarksSequentially(benchmarkDir, solvedFile);

        System.out.println("\n=== Statistiche Generate ===");
        benchmarks.forEach(b -> {
            System.out.println("Benchmark: " + b.getBenchmarkName());
            System.out.println(" - Sparsity Index: " + b.getSparsityIndex());
            System.out.println(" - Rows: " + b.getRows());
            System.out.println(" - Columns: " + b.getColomuns());
            System.out.println(" - Empty Columns: " + b.getEmptyColumns());
            System.out.println(" - Average 1s per Row: " + b.getAvarageOnePerRow());
            System.out.println(" - Max 1s per Row: " + b.getMaxOnePerRow());
            System.out.println(" - Min 1s per Row: " + b.getMinOnePerRow());
            System.out.println(" - Std. Dev (1s per Row): " + b.getDistribution());
            System.out.println();
        });
    }

    private Path prepareSelectedFile(String destDir) throws IOException {
        Path dirPath = Paths.get(destDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path solvedFile = dirPath.resolve(SELECTED_FILENAME);
        Files.deleteIfExists(solvedFile);
        return Files.createFile(solvedFile);
    }

    private void processBenchmarksSequentially(String benchmarkDir, Path solvedFile) throws IOException {
        try (Stream<Path> filesStream = Files.list(Paths.get(benchmarkDir))) {
            filesStream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(path -> processSingleBenchmark(path, solvedFile));
        }
    }

    private void processSingleBenchmark(Path inputFile, Path solvedFile) {
        String filename = inputFile.getFileName().toString();
        System.out.println("[Processing] " + filename);

        boolean[][] instance = BenchmarkReader.readBenchmark(inputFile.toString());

        float sparsity = computeSparsity(instance);
        int rows = instance.length;
        int cols = instance[0].length;
        int emptyCols = computeEmptyColumns(instance);
        float avg = computeAverageOnePerRow(instance);
        float max = computeMaxOnePerRow(instance);
        float min = computeMinOnePerRow(instance);
        float std = computeDistribution(instance);

        BenchmarksStatistics b = new BenchmarksStatistics(
                filename, sparsity, rows, cols, emptyCols, avg, max, min, std
        );
        this.benchmarks.add(b);

        writeSelectedBenchmark(filename, solvedFile);
    }

    private void writeSelectedBenchmark(String filename, Path solvedFile) {
        try {
            Files.writeString(solvedFile, filename + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[Error] Failed to write " + filename);
        }
    }

    // === ANALISI MATRICE ===

    public float computeSparsity(boolean[][] instance) {
        int ones = 0;
        int total = instance.length * instance[0].length;
        for (boolean[] row : instance) {
            for (boolean cell : row) {
                if (cell) ones++;
            }
        }
        return 1.0f - ((float) ones / total);
    }

    public int computeEmptyColumns(boolean[][] instance) {
        int cols = instance[0].length;
        int emptyCols = 0;

        for (int j = 0; j < cols; j++) {
            boolean hasOne = false;
            for (boolean[] row : instance) {
                if (row[j]) {
                    hasOne = true;
                    break;
                }
            }
            if (!hasOne) emptyCols++;
        }

        return emptyCols;
    }

    public float computeAverageOnePerRow(boolean[][] instance) {
        int total = 0;
        for (boolean[] row : instance) {
            for (boolean b : row) {
                if (b) total++;
            }
        }
        return (float) total / instance.length;
    }

    public float computeMaxOnePerRow(boolean[][] instance) {
        int max = 0;
        for (boolean[] row : instance) {
            int count = 0;
            for (boolean b : row) {
                if (b) count++;
            }
            if (count > max) max = count;
        }
        return max;
    }

    public float computeMinOnePerRow(boolean[][] instance) {
        int min = Integer.MAX_VALUE;
        for (boolean[] row : instance) {
            int count = 0;
            for (boolean b : row) {
                if (b) count++;
            }
            if (count < min) min = count;
        }
        return min;
    }

    public float computeDistribution(boolean[][] instance) {
        int n = instance.length;
        float mean = computeAverageOnePerRow(instance);
        float varianceSum = 0;

        for (boolean[] row : instance) {
            int count = 0;
            for (boolean b : row) {
                if (b) count++;
            }
            varianceSum += Math.pow(count - mean, 2);
        }

        return (float) Math.sqrt(varianceSum / n);
    }
}