package unibs.asd.benchmarks;

import java.io.IOException;

public class StatisticsApp {
    public static void main(String[] args) {
        String benchmarkDirectory = "src\\mybenchmarks";
        String outputDirectory = "results\\results.csv";

        Analyzer analyzer = new Analyzer();
        try {
            analyzer.analyzeBenchmarks(benchmarkDirectory, outputDirectory);
        } catch (IOException e) {
            System.out.println("Error during benchmark analysis: " + e.getMessage());
        }

    }
}
