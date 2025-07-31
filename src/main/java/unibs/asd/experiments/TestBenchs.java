package unibs.asd.experiments;

import unibs.asd.enums.BitSetType;

public class TestBenchs {
    public static void main(String[] args) {
        String benchmarkDir = "src\\mybenchmarks";
        String destDir = "results\\5000ms";
        String solvedFilename = "solved_benchmarks_5000ms.txt";
        int timeoutMs = 30_000; // 10 seconds

        try {
            FilterBenchmarks filterBenchmarks = new FilterBenchmarks(
                benchmarkDir, destDir, solvedFilename, timeoutMs, BitSetType.FAST_BITSET);
            filterBenchmarks.runBenchmarks();
            System.out.println("Benchmarks processed successfully.");
        } catch (Exception e) {
            System.err.println("Error processing benchmarks: " + e.getMessage());
        }
    }
}
