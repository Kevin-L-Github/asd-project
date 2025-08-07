package unibs.asd.experiments;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for executing benchmarks with multiple timeout levels.
 * Processes benchmarks in increasing timeout categories, skipping already solved ones.
 */
public class TestBenchs {
    /**
     * Main entry point for the benchmark execution.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        String benchmarkDir = "src\\mybenchmarks";
        
        // We define the timeout levels and their output cartridges
        List<BenchmarkLevel> levels = new ArrayList<>();
        levels.add(new BenchmarkLevel(1_000, "results\\1000ms", "solved_benchmarks_1000ms.txt"));
        levels.add(new BenchmarkLevel(30_000, "results\\30_000ms", "solved_benchmarks_30_000ms.txt"));
        levels.add(new BenchmarkLevel(300_000, "results\\300_000ms", "solved_benchmarks_300_000ms.txt"));
        levels.add(new BenchmarkLevel(600_000, "results\\600_000ms", "solved_benchmarks_600_000ms.txt"));

        // We run benchmarks for each level
        for (int i = 0; i < levels.size(); i++) {
            BenchmarkLevel currentLevel = levels.get(i);
            System.out.println("Processing benchmarks with timeout " + currentLevel.getTimeoutMs() + "ms");
            
            // Prepare the list of files already resolved in previous levels
            List<String> alreadySolvedFiles = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                alreadySolvedFiles.add(levels.get(j).getSolvedFilePath());
            }
            
            try {
                FilterBenchmarks filterBenchmarks = new FilterBenchmarks(
                    benchmarkDir, 
                    currentLevel.getDestDir(), 
                    currentLevel.getSolvedFilename(), 
                    currentLevel.getTimeoutMs(), 
                    alreadySolvedFiles);
                
                filterBenchmarks.runBenchmarks();
                System.out.println("Benchmarks processed successfully for timeout " + currentLevel.getTimeoutMs() + "ms");
            } catch (Exception e) {
                System.err.println("Error processing benchmarks for timeout " + currentLevel.getTimeoutMs() + "ms: " + e.getMessage());
            }
        }
    }
}