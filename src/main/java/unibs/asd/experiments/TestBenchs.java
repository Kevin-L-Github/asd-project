package unibs.asd.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestBenchs {
    public static void main(String[] args) {
        String benchmarkDir = "src\\mybenchmarks";
        String destDir = "results\\30_000ms";
        String solvedFilename = "solved_benchmarks_30_000ms.txt";
        int timeoutMs = 30_000; // milliseconds

        List<String> alreadySolvedFiles = new ArrayList<>();
        alreadySolvedFiles.add("results\\1000ms\\solved_benchmarks_1000ms.txt");

        try {
            FilterBenchmarks filterBenchmarks = new FilterBenchmarks(
                benchmarkDir, destDir, solvedFilename, timeoutMs, alreadySolvedFiles);
            filterBenchmarks.runBenchmarks();
            System.out.println("Benchmarks processed successfully.");
        } catch (Exception e) {
            System.err.println("Error processing benchmarks: " + e.getMessage());
        }
    }
}
