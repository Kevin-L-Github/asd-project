package unibs.asd.project;

import java.io.IOException;

public class BooleanApp {

        public static final String DESTINATION = "results";
    public static void main(String[] args) {
        String benchmarkDir = "src/benchmarks1";
        int maxFilesToProcess = 5;

        try {
            Experiment.runBenchmarks(benchmarkDir,  DESTINATION, maxFilesToProcess);
        } catch (IOException e) {
            System.err.println("Error processing benchmark files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}