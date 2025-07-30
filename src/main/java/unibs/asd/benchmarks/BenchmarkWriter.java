package unibs.asd.benchmarks;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;

import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.MHS;

public class BenchmarkWriter {

    public static void writeBenchmark(MHS mhs, String filename, String destDir) throws IOException {
        if (!mhs.isExecuted()) {
            throw new IllegalArgumentException("L'algoritmo non è stato eseguito!");
        }

        String outputFilename = filename.replaceAll("\\.matrix$", ".mhs");

        List<Hypothesis> solutions = mhs.getSolutions();
        boolean[][] instance = mhs.getInstance();
        int nonEmptyColumns = mhs.getNonEmptyColumns().size();
        int emptyCols = instance[0].length - nonEmptyColumns;
        int numSolutions = solutions.size();
        double computationTimeNs = mhs.getComputationTime();
        boolean stopped = mhs.isStopped();

        Path outputPath = Paths.get(destDir, outputFilename);

        int minCardinality = solutions.stream()
            .mapToInt(Hypothesis::cardinality)
            .min()
            .orElse(0);

        int maxCardinality = solutions.stream()
            .mapToInt(Hypothesis::cardinality)
            .max()
            .orElse(0);

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write("Soluzioni:\n");
            for (Hypothesis sol : solutions) {
                writer.write(sol.toString());
                writer.write("\n");
            }

            writer.write(";;; Number of solutions: " + numSolutions + "\n");
            writer.write(";;; Min Cardinality: " + minCardinality + "\n");
            writer.write(";;; Max Cardinality: " + maxCardinality + "\n");
            writer.write(";;; Matrix dimensions (N x M): " + instance.length + " x " + instance[0].length + "\n");
            writer.write(";;; Empty columns: " + emptyCols + "\n");
            writer.write(";;; Time Taken: " + formatTime(computationTimeNs) + "\n");
            writer.write(";;; Status: " + (stopped ? "Stopped within time" : "Algorithm completed") + "\n");
        }
    }

    private static String formatTime(double timeNs) {
        DecimalFormat df = new DecimalFormat("0.###");

        if (timeNs < 1_000) {
            return df.format(timeNs) + " ns";
        } else if (timeNs < 1_000_000) {
            return df.format(timeNs / 1_000) + " µs";
        } else if (timeNs < 1_000_000_000) {
            return df.format(timeNs / 1_000_000) + " ms";
        } else {
            return df.format(timeNs / 1_000_000_000) + " s";
        }
    }
}
