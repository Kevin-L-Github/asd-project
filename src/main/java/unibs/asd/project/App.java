package unibs.asd.project;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        String benchmarkDir = "src/benchmarks1";
        String outputFile = "benchmark_results.txt";
        int maxFilesToProcess = 5; // Limite di file da processare

        try {
            Experiment.runBenchmarks(benchmarkDir, outputFile, maxFilesToProcess);
            System.out.println("Processing completed. Results saved to " + outputFile);
        } catch (IOException e) {
            System.err.println("Error processing benchmark files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}