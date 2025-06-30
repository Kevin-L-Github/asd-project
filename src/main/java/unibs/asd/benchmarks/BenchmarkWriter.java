package unibs.asd.benchmarks;

import java.util.List;

import unibs.asd.interfaces.Hypothesis;
import unibs.asd.mhs.BaseMHS;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class BenchmarkWriter {

    /**
     * 
     * @param mhs      instance of an already executed mhs solver
     * @param filename name of the input file with .matrix extension
     * @param destDir  destination directory (in project root)
     */
    public static void writeBenchmark(BaseMHS mhs, String filename, String destDir) {
        if (!mhs.isExecuted()) {
            throw new IllegalArgumentException("L'algoritmo non è stato eseguito!");
        }

        // Convert filename from .matrix to .mhs
        String outputFilename = filename.replace(".matrix", ".mhs");

        List<Hypothesis> solutions = mhs.getSolutions();
        boolean[][] instance = mhs.getInstance();
        int nonEmptyColumns = mhs.getNonEmptyColumns().size();
        int emptyCols = instance[0].length - nonEmptyColumns;
        int numSolutions = solutions.size();
        double computationTime = mhs.getComputationTime(); // in nanoseconds
        boolean stopped = mhs.isStopped();

        // Create full path including destination directory
        Path outputPath = Paths.get(destDir, outputFilename);

        try (FileWriter writer = new FileWriter(outputPath.toString())) {
            // Write solutions
            writer.write("Soluzioni:\n");
            for (Hypothesis sol : solutions) {
                writer.write(sol.toString() + "\n");
            }

            // Write metadata
            writer.write(";;; Number of solutions: " + numSolutions + "\n");

            // Calculate min and max cardinality
            int minCardinality = Integer.MAX_VALUE;
            int maxCardinality = Integer.MIN_VALUE;
            for (Hypothesis sol : solutions) {
                int card = sol.cardinality();
                if (card < minCardinality)
                    minCardinality = card;
                if (card > maxCardinality)
                    maxCardinality = card;
            }
            writer.write(";;; Min Cardinality: " + minCardinality + "\n");
            writer.write(";;; Max Cardinality: " + maxCardinality + "\n");

            // Matrix dimensions
            writer.write(";;; Matrix dimensions (N x M): " + instance.length + " x " + instance[0].length + "\n");

            // Empty columns
            writer.write(";;; Empty columns: " + emptyCols + "\n");

            // Format time
            DecimalFormat df = new DecimalFormat("0.###");
            String timeNs = df.format(computationTime) + "ns";
            String timeMs = df.format(computationTime / 1_000_000) + "ms";
            String timeS = df.format(computationTime / 1_000_000_000) + "s";
            writer.write(";;; Time Taken: " + timeNs + " / " + timeMs + " / " + timeS + "\n");

            // Completion status
            writer.write(";;; " + (stopped ? "Stopped within time" : "The algorithm was completed") + "\n");

        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file: " + e.getMessage());
        }
    }
}