package unibs.asd.bools;

import java.io.IOException;
import java.nio.file.Paths;

import unibs.asd.benchmarks.FilterBenchmarks;

public class BoolsMain {

    public static final String DESTINATION = "results0";

    public static void main(String[] args) {
        // Verifica se da riga di comando sono stati specificati directory e num file, altrimenti => default
        String benchmarkDir = args.length > 0 ? args[0] : "src/mybenchmarks";
        // int maxFilesToProcess = args.length > 1 ? Integer.parseInt(args[1]) : 5;

        System.out.println("Starting benchmarks with settings:");
        System.out.println("Directory: " + Paths.get(benchmarkDir).toAbsolutePath());
        System.out.println("Output directory: " + DESTINATION);

        try {
            FilterBenchmarks.runBenchmarks(benchmarkDir, DESTINATION);
            System.out.println("\nAll benchmarks completed successfully");
        } catch (IOException e) {
            System.err.println("Error processing benchmark files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}